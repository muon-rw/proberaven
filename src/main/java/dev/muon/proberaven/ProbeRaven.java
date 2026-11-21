package dev.muon.proberaven;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProbeRaven {
    public static final String MOD_ID = "proberaven";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = /*$ mod_version*/"0.1.0";
    public static final String MINECRAFT = /*$ minecraft*/"26.3";

    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "proberaven-io");
        thread.setDaemon(true);
        return thread;
    });

    public static boolean isExitAfterDump() {
        if (Boolean.getBoolean("proberaven.exitAfterDump")) {
            return true;
        }
        String env = System.getenv("PROBERAVEN_EXIT_AFTER_DUMP");
        return "true".equalsIgnoreCase(env) || "1".equals(env);
    }

    public static void dumpAsync(MinecraftServer server) {
        if (isExitAfterDump()) {
            return;
        }
        long startNanos = System.nanoTime();
        DumpSnapshot snapshot = RegistryDumper.capture(server.registryAccess());
        Path outputPath = RegistryDumper.resolveOutputPath();
        IO_EXECUTOR.submit(() -> {
            try {
                Path written = RegistryDumper.writeAtomic(snapshot, outputPath);
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
                LOGGER.info("ProbeRaven dump complete: {} ({}, {}ms)", written, snapshot.summary(), elapsedMs);
            } catch (IOException e) {
                LOGGER.error("Failed to write ProbeRaven dump to {}", outputPath, e);
            }
        });
    }

    public static void dumpAndHalt(MinecraftServer server) {
        long startNanos = System.nanoTime();
        try {
            DumpSnapshot snapshot = RegistryDumper.capture(server.registryAccess());
            Path written = RegistryDumper.writeAtomic(snapshot, RegistryDumper.resolveOutputPath());
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
            LOGGER.info("ProbeRaven dump complete: {} ({}, {}ms)", written, snapshot.summary(), elapsedMs);
            System.out.flush();
            System.err.flush();
            Runtime.getRuntime().halt(0);
        } catch (Throwable t) {
            LOGGER.error("ProbeRaven headless dump failed", t);
            System.out.flush();
            System.err.flush();
            Runtime.getRuntime().halt(1);
        }
    }
}
