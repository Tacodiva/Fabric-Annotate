package sh.emberj.annotate.core.mixin;

import java.io.File;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.fabricmc.loader.impl.game.minecraft.Hooks;
import net.minecraft.client.MinecraftClient;
import sh.emberj.annotate.core.AnnotateEntrypoint;

@Mixin(MinecraftClient.class)
public class ClientPreinitHookMixin {
    @Redirect(method = "<init>(Lnet/minecraft/client/RunArgs;)V", at = @At(value = "INVOKE", target = "Lnet/fabricmc/loader/impl/game/minecraft/Hooks;startClient(Ljava/io/File;Ljava/lang/Object;)V"))
    private void startClient(File runDir, Object gameInstance) {
        AnnotateEntrypoint entrypoints = AnnotateEntrypoint.getInstance();
        entrypoints.onPreInitialize();
        Hooks.startClient(runDir, gameInstance);
        entrypoints.onPostInitialize();
    }
}
