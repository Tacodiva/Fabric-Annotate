package sh.emberj.annotate.core.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.TitleScreen;
import sh.emberj.annotate.core.Annotate;

@Mixin(TitleScreen.class)
public class TestInject {
	@Inject(at = @At("TAIL"), method = "init()V")
	private void init(CallbackInfo info) {
		Annotate.LOG.info("This line is printed by an example mod mixin!");
        System.exit(1);
	}
}
