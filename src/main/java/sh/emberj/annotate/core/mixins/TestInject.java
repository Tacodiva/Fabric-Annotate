package sh.emberj.annotate.core.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screen.TitleScreen;

@Mixin(TitleScreen.class)
public class TestInject {
	@Inject(at = @At("TAIL"), method = "init()V")
	private void init(double abcde, CallbackInfoReturnable<Double> info) {
		info.setReturnValue(memberOne(abcde, info, info.getReturnValue()));
	}

	public double memberOne(double abcde, CallbackInfoReturnable<Double> info, double blah) {
		return -1;
	}
}
