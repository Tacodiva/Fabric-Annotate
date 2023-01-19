package sh.emberj.annotate.core;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import sh.emberj.annotate.test.MixinTarget;

public class AnnotateEntrypoint implements ModInitializer, PreLaunchEntrypoint {

	public static final AnnotateEntrypoint INSTANCE = new AnnotateEntrypoint();

	@Override
	public void onPreLaunch() {
		// try {
		// 	ClassMetadataFactory.create(Type.getType(MixinTarget.class));
		// } catch (AnnotateException e) {
		// 	e.printStackTrace();
		// }
		Annotate.updateLoadStage(AnnotateLoadStage.PRELAUNCH);
		MixinTarget.staticOne();
		System.out.println(MixinTarget.staticTwo("blah", 8972));
		System.exit(0);
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
