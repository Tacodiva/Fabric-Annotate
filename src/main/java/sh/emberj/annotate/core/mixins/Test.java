package sh.emberj.annotate.core.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import sh.emberj.annotate.alloy.mixinext.AlloyInject;
import sh.emberj.annotate.test.MixinTarget;

@Mixin(MixinTarget.class)
public class Test {

    @AlloyInject(method = "staticOne", at = @At("HEAD") )
    private static void test() {
        System.out.println("Hello.");
    }
}
