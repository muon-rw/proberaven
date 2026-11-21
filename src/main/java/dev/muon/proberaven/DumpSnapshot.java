package dev.muon.proberaven;

import java.util.List;
import java.util.Map;

public record DumpSnapshot(
    int formatVersion,
    String minecraft,
    String loader,
    List<String> mods,
    Map<String, List<String>> registries,
    Map<String, List<String>> tags,
    Map<String, Map<String, List<String>>> blockStates
) {
    public static final int FORMAT_VERSION = 1;

    public int entryCount() {
        int total = 0;
        for (List<String> ids : registries.values()) {
            total += ids.size();
        }
        return total;
    }

    public int tagCount() {
        int total = 0;
        for (List<String> ids : tags.values()) {
            total += ids.size();
        }
        return total;
    }

    public String summary() {
        return registries.size() + " registries, "
            + entryCount() + " entries, "
            + tagCount() + " tags, "
            + blockStates.size() + " blockStates, "
            + mods.size() + " mods";
    }
}
