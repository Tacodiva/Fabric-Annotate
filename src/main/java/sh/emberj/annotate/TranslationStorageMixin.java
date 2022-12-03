package sh.emberj.annotate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.resource.language.TranslationStorage;

@Mixin(TranslationStorage.class)

public class TranslationStorageMixin {
    @Inject(at = @At("RETURN"), method = "init()V")
    private void init(CallbackInfo info) {
        
        // ExampleMod.LOGGER.info("This line is printed by an example mod mixin!");
    }
}
