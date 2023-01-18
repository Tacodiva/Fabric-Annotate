package sh.emberj.annotate.core;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import sh.emberj.annotate.test.MixinTarget;

public class AnnotateEntrypoint implements ModInitializer, PreLaunchEntrypoint {

	public static final AnnotateEntrypoint INSTANCE = new AnnotateEntrypoint();

	@Override
	public void onPreLaunch() {
		Annotate.updateLoadStage(AnnotateLoadStage.PRELAUNCH);
		MixinTarget.staticTwo("HELLO", 102809);
	}

	public void onPreInitialize() {
		Annotate.updateLoadStage(AnnotateLoadStage.PREINIT);
	}

	@Override
	public void onInitialize() {
		Annotate.updateLoadStage(AnnotateLoadStage.INIT);
	}

	public void onPostInitialize() {
		Annotate.updateLoadStage(AnnotateLoadStage.POSTINIT);
	}
}
