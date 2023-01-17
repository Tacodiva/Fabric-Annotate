package sh.emberj.annotate.resource.mixins;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.resource.language.TranslationStorage;

@Mixin(TranslationStorage.class)
public class TranslationStorageMixin {

    @Redirect(method = "load", at = @At(value = "INVOKE", desc = @Desc(owner = ImmutableMap.class, value = "copyOf", ret = ImmutableMap.class, args = Map.class)))
    private static ImmutableMap<String, String> copyOf(Map<String, String> translations) {
        // int beforeSize = translations.size();
        // Translation.TranslationManager.applyTranslations(translations);
        // int delta = translations.size() - beforeSize;
        // if (delta != 0) {
        //     Annotate.LOG.info("Injected " + delta + " translations!");
        // }
        return ImmutableMap.copyOf(translations);
    }

}
