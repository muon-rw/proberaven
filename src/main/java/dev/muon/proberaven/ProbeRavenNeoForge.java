package dev.muon.proberaven;

//? if neoforge {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod(ProbeRaven.MOD_ID)
public class ProbeRavenNeoForge {
    public ProbeRavenNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> ProbeRaven.dumpAsync(event.getServer()));
        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> {
            if (event.getPlayer() == null) {
                ProbeRaven.dumpAsync(event.getPlayerList().getServer());
            }
        });
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
            ProbeRavenCommands.register(event.getDispatcher())
        );
    }
}
*///?}
