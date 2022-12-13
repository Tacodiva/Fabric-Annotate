package sh.emberj.annotate.test;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateScan;
import sh.emberj.annotate.mixin.MixinMethodHead;
import sh.emberj.annotate.mixin.MixinMethodTail;

@AnnotateScan
public class Test implements ModInitializer {

    @Override
    public void onInitialize() {
        AnimalRegistry.INSTANCE.get(new Identifier("annotate:pig")).makeNoise();
        AnimalRegistry.INSTANCE.get(new Identifier("annotate:sheep")).makeNoise();
        AnimalRegistry.INSTANCE.get(new Identifier("annotate:piglet")).makeNoise();
    }

    @MixinMethodHead(type = MixinTarget.class)
    public static void staticOne() {
        Annotate.LOG.info("Static One Mixin!");
    }

    @MixinMethodHead(type = MixinTarget.class)
    public static String staticTwo(String idk, int fbfb, CallbackInfo info) {
        Annotate.LOG.info("Static Two Mixin! Got idk = " + idk + " and fbfb = " + fbfb);
        return "cancelled!";
    }

    @MixinMethodTail(type = MixinTarget.class)
    public static double memberOne(MixinTarget _this, double abcde, CallbackInfo cbi, double returnVal) {
        Annotate.LOG.info("Member One Mixin! " + abcde);
        Annotate.LOG.info("State = " + _this.state);
        Annotate.LOG.info("State = " + cbi.getId());
        Annotate.LOG.info(""+returnVal);
        return 1111;
    }
}
