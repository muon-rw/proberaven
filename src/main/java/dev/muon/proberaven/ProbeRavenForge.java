package dev.muon.proberaven;

//? if forge {
/*import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(ProbeRaven.MOD_ID)
public class ProbeRavenForge {
    public ProbeRavenForge() {
        MinecraftForge.EVENT_BUS.addListener((ServerStartedEvent event) -> ProbeRaven.dumpAsync(event.getServer()));
        MinecraftForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> {
            if (event.getPlayer() == null) {
                ProbeRaven.dumpAsync(event.getPlayerList().getServer());
            }
        });
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
            ProbeRavenCommands.register(event.getDispatcher())
        );
    }
}
*///?}
