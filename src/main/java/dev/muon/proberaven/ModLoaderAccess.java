package dev.muon.proberaven;

import java.nio.file.Path;
import java.util.List;

public sealed interface ModLoaderAccess {
    ModLoaderAccess INSTANCE =
        /*? if fabric{*/new FabricLoaderAccess();
        /*?} elif neoforge {*//*new NeoForgeLoaderAccess();
        *//*?} elif forge *///new ForgeLoaderAccess();

    boolean isClient();
    boolean isServer();
    boolean isModLoaded(String id);
    String getLoaderName();
    Path getGameDir();
    List<String> getLoadedModIds();

    //? if fabric {
    final class FabricLoaderAccess implements ModLoaderAccess {
        private final net.fabricmc.loader.api.FabricLoader loader = net.fabricmc.loader.api.FabricLoader.getInstance();

        @Override
        public boolean isClient() {
            return loader.getEnvironmentType().equals(net.fabricmc.api.EnvType.CLIENT);
        }

        @Override
        public boolean isServer() {
            return loader.getEnvironmentType().equals(net.fabricmc.api.EnvType.SERVER);
        }

        @Override
        public boolean isModLoaded(String id) {
            return loader.isModLoaded(id);
        }

        @Override
        public String getLoaderName() {
            return "fabric";
        }

        @Override
        public Path getGameDir() {
            return loader.getGameDir();
        }

        @Override
        public List<String> getLoadedModIds() {
            return loader.getAllMods().stream()
                .map(mod -> mod.getMetadata().getId())
                .distinct()
                .sorted()
                .toList();
        }
    }
    //?} elif neoforge {
    /*final class NeoForgeLoaderAccess implements ModLoaderAccess {
        private final net.neoforged.api.distmarker.Dist dist =
            /^? if >=1.21.9 {^/net.neoforged.fml.loading.FMLEnvironment.getDist();
            /^?} else^///net.neoforged.fml.loading.FMLEnvironment.dist;
        private final net.neoforged.fml.loading.LoadingModList mods =
            /^? if >=1.21.9 {^/net.neoforged.fml.loading.FMLLoader.getCurrent().getLoadingModList();
            /^?} else^///net.neoforged.fml.loading.FMLLoader.getLoadingModList();

        @Override
        public boolean isClient() {
            return dist.isClient();
        }

        @Override
        public boolean isServer() {
            return dist.isDedicatedServer();
        }

        @Override
        public boolean isModLoaded(String id) {
            return mods.getModFileById(id) != null;
        }

        @Override
        public String getLoaderName() {
            return "neoforge";
        }

        @Override
        public Path getGameDir() {
            return net.neoforged.fml.loading.FMLPaths.GAMEDIR.get();
        }

        @Override
        public List<String> getLoadedModIds() {
            return net.neoforged.fml.ModList.get().getMods().stream()
                .map(net.neoforged.neoforgespi.language.IModInfo::getModId)
                .distinct()
                .sorted()
                .toList();
        }
    }
    *///?} elif forge {
    /*final class ForgeLoaderAccess implements ModLoaderAccess {
        private final net.minecraftforge.api.distmarker.Dist dist = net.minecraftforge.fml.loading.FMLEnvironment.dist;
        private final net.minecraftforge.fml.loading.LoadingModList mods = net.minecraftforge.fml.loading.FMLLoader.getLoadingModList();

        @Override
        public boolean isClient() {
            return dist.isClient();
        }

        @Override
        public boolean isServer() {
            return dist.isDedicatedServer();
        }

        @Override
        public boolean isModLoaded(String id) {
            return mods.getModFileById(id) != null;
        }

        @Override
        public String getLoaderName() {
            return "forge";
        }

        @Override
        public Path getGameDir() {
            return net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get();
        }

        @Override
        public List<String> getLoadedModIds() {
            return net.minecraftforge.fml.ModList.get().getMods().stream()
                .map(net.minecraftforge.forgespi.language.IModInfo::getModId)
                .distinct()
                .sorted()
                .toList();
        }
    }
    *///?}
}
