package sh.emberj.annotate.test;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.mixin.MixinMethodHead;

public class Test implements ModInitializer {

    @Override
    public void onInitialize() {
        AnimalRegistry.INSTANCE.get(new Identifier("annotate:pig")).makeNoise();
        AnimalRegistry.INSTANCE.get(new Identifier("annotate:sheep")).makeNoise();
        AnimalRegistry.INSTANCE.get(new Identifier("annotate:piglet")).makeNoise();
    }

    @MixinMethodHead(type = TitleScreen.class)
    public static void init() {

    }
}
