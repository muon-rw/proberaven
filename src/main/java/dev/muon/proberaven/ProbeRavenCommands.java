package dev.muon.proberaven;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Path;

public final class ProbeRavenCommands {
    private ProbeRavenCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("proberaven")
                //? if >=1.21.11 {
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                //?} else {
                /*.requires(source -> source.hasPermission(2))
                *///?}
                .then(Commands.literal("dump").executes(context -> executeDump(context.getSource())))
        );
    }

    private static int executeDump(CommandSourceStack source) {
        long startNanos = System.nanoTime();
        DumpSnapshot snapshot = RegistryDumper.capture(source.getServer().registryAccess());
        Path outputPath = RegistryDumper.resolveOutputPath();
        try {
            Path written = RegistryDumper.writeAtomic(snapshot, outputPath);
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
            String message = "ProbeRaven dumped " + snapshot.summary() + " to " + written + " (" + elapsedMs + "ms)";
            source.sendSuccess(() -> Component.literal(message), false);
            return 1;
        } catch (IOException e) {
            ProbeRaven.LOGGER.error("Failed to write ProbeRaven dump to {}", outputPath, e);
            source.sendFailure(Component.literal("ProbeRaven dump failed: " + e.getMessage()));
            return 0;
        }
    }
}
