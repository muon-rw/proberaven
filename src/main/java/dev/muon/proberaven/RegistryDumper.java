package dev.muon.proberaven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public final class RegistryDumper {
    private static final String MINECRAFT_PREFIX = "minecraft:";
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private RegistryDumper() {
    }

    public static Path resolveOutputPath() {
        String override = System.getProperty("proberaven.output");
        if (override == null || override.isBlank()) {
            override = System.getenv("PROBERAVEN_OUTPUT");
        }
        if (override != null && !override.isBlank()) {
            return Path.of(override.trim());
        }
        return ModLoaderAccess.INSTANCE.getGameDir()
            .resolve(".proberaven")
            .resolve("dump.json");
    }

    public static DumpSnapshot capture(RegistryAccess registryAccess) {
        Map<String, TreeSet<String>> registryEntries = new TreeMap<>();
        Map<String, TreeSet<String>> tagEntries = new TreeMap<>();

        registryAccess.registries().forEach(entry ->
            collectRegistry(entry.value(), registryEntries, tagEntries)
        );
        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            collectRegistry(registry, registryEntries, tagEntries);
        }

        //? if forge {
        /*// Include Forge registries that do not have a vanilla wrapper in BuiltInRegistries.REGISTRY.
        for (Identifier regName : net.minecraftforge.registries.RegistryManager.ACTIVE.takeSnapshot(false).keySet()) {
            net.minecraftforge.registries.ForgeRegistry<?> forgeReg = net.minecraftforge.registries.RegistryManager.ACTIVE.getRegistry(regName);
            if (forgeReg == null) {
                continue;
            }
            String key = normalizeRegistryKey(regName);
            TreeSet<String> entries = registryEntries.computeIfAbsent(key, ignored -> new TreeSet<>());
            for (Identifier id : forgeReg.getKeys()) {
                entries.add(id.toString());
            }
            net.minecraftforge.registries.tags.ITagManager<?> tagManager = forgeReg.tags();
            if (tagManager != null) {
                tagManager.getTagNames().forEach(tagKey ->
                    addTag(tagEntries, key, tagKey.location().toString())
                );
            }
        }
        *///?}

        Map<String, List<String>> registries = new TreeMap<>();
        for (Map.Entry<String, TreeSet<String>> entry : registryEntries.entrySet()) {
            registries.put(entry.getKey(), List.copyOf(entry.getValue()));
        }

        Map<String, List<String>> tags = new TreeMap<>();
        for (Map.Entry<String, TreeSet<String>> entry : tagEntries.entrySet()) {
            tags.put(entry.getKey(), List.copyOf(entry.getValue()));
        }

        Map<String, Map<String, List<String>>> blockStates = collectBlockStates();
        List<String> mods = ModLoaderAccess.INSTANCE.getLoadedModIds();

        return new DumpSnapshot(
            DumpSnapshot.FORMAT_VERSION,
            ProbeRaven.MINECRAFT,
            ModLoaderAccess.INSTANCE.getLoaderName(),
            mods,
            registries,
            tags,
            blockStates
        );
    }

    public static Path writeAtomic(DumpSnapshot snapshot, Path outputPath) throws IOException {
        Path normalized = outputPath.toAbsolutePath().normalize();
        Path parent = normalized.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path tmp = normalized.resolveSibling(normalized.getFileName() + ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            GSON.toJson(snapshot, writer);
            writer.flush();
        }

        try {
            Files.move(tmp, normalized, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(tmp, normalized, StandardCopyOption.REPLACE_EXISTING);
        }
        return normalized;
    }

    private static void collectRegistry(
        Registry<?> registry,
        Map<String, TreeSet<String>> registryEntries,
        Map<String, TreeSet<String>> tagEntries
    ) {
        String key = normalizeRegistryKey(resourceKeyId(registry.key()));
        TreeSet<String> entries = registryEntries.computeIfAbsent(key, ignored -> new TreeSet<>());
        for (Identifier id : registry.keySet()) {
            entries.add(id.toString());
        }

        //? if >=1.21.2 {
        registry.getTags().forEach(named -> addTag(tagEntries, key, named.key().location().toString()));
        //?} else {
        /*registry.getTagNames().forEach(tagKey -> addTag(tagEntries, key, tagKey.location().toString()));
        *///?}
    }

    private static void addTag(Map<String, TreeSet<String>> tagEntries, String registryKey, String tagId) {
        tagEntries.computeIfAbsent(registryKey, ignored -> new TreeSet<>()).add(tagId);
    }

    private static Map<String, Map<String, List<String>>> collectBlockStates() {
        Map<String, Map<String, List<String>>> blockStates = new TreeMap<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            if (id == null) {
                continue;
            }
            Map<String, List<String>> properties = new LinkedHashMap<>();
            for (Property<?> property : block.getStateDefinition().getProperties()) {
                properties.put(property.getName(), collectPropertyValues(property));
            }
            blockStates.put(id.toString(), properties);
        }
        return blockStates;
    }

    private static <T extends Comparable<T>> List<String> collectPropertyValues(Property<T> property) {
        List<String> values = new ArrayList<>();
        for (T value : property.getPossibleValues()) {
            values.add(property.getName(value));
        }
        return values;
    }

    private static Identifier resourceKeyId(ResourceKey<?> key) {
        //? if >=1.21.11 {
        return key.identifier();
        //?} else {
        /*return key.location();
        *///?}
    }

    private static String normalizeRegistryKey(Identifier id) {
        String raw = id.toString();
        if (raw.startsWith(MINECRAFT_PREFIX)) {
            return raw.substring(MINECRAFT_PREFIX.length());
        }
        return raw;
    }
}
