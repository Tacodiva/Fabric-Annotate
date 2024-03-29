package sh.emberj.annotate.core.mixins;

import java.io.File;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.fabricmc.loader.impl.game.minecraft.Hooks;
import sh.emberj.annotate.core.AnnotateEntrypoint;

@Mixin(net.minecraft.server.Main.class)
public class ServerPreinitHookMixin {
    @Redirect(method = "main([Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/fabricmc/loader/impl/game/minecraft/Hooks;startServer(Ljava/io/File;Ljava/lang/Object;)V", remap = false))
    private static void startServer(File runDir, Object gameInstance) {
        AnnotateEntrypoint entrypoints = AnnotateEntrypoint.INSTANCE;
        entrypoints.onPreInitialize();
        Hooks.startServer(runDir, gameInstance);
        entrypoints.onPostInitialize();
    }
}
