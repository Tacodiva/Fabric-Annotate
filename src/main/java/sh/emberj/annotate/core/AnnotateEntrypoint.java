package sh.emberj.annotate.core;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class AnnotateEntrypoint implements ModInitializer, PreLaunchEntrypoint {

	private static AnnotateEntrypoint _instance;

	public static AnnotateEntrypoint getInstance() {
		return _instance;
	}

	@Override
	public void onPreLaunch() {
		// CoolTesting.run();
		_instance = this;

		Annotate.setLoadStage(LoadStage.PRELAUNCH);
	}

	public void onPreInitialize() {
		Annotate.setLoadStage(LoadStage.PREINIT);
	}

	@Override
	public void onInitialize() {
		Annotate.setLoadStage(LoadStage.INIT);
	}

	public void onPostInitialize() {
		Annotate.setLoadStage(LoadStage.POSTINIT);
	}
}
