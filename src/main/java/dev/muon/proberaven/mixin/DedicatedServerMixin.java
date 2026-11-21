package dev.muon.proberaven.mixin;

import dev.muon.proberaven.ProbeRaven;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin {
    // Dump before TCP binding, keypair init, and loadLevel() when running headless for Packraven.
    @Inject(method = "initServer", at = @At("HEAD"))
    private void proberaven$dumpAndExitHeadless(CallbackInfoReturnable<Boolean> cir) {
        if (ProbeRaven.isExitAfterDump()) {
            ProbeRaven.dumpAndHalt((MinecraftServer) (Object) this);
        }
    }
}
