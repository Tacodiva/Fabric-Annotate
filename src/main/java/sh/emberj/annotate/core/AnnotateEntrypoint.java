package sh.emberj.annotate.core;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import sh.emberj.annotate.test.MixinTarget;

public class AnnotateEntrypoint implements ModInitializer, PreLaunchEntrypoint {

	private static AnnotateEntrypoint _instance;

	public static AnnotateEntrypoint getInstance() {
		return _instance;
	}

	@Override
	public void onPreLaunch() {
		_instance = this;
		// try {
		// ClassReader reader = new
		// ClassReader("sh.emberj.annotate.core.mixin.TestInject");
		// TraceClassVisitor tcv = new TraceClassVisitor(new PrintWriter(System.out));
		// reader.accept(tcv, 0);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		Annotate.setLoadStage(LoadStage.PRELAUNCH);

		MixinTarget.staticOne();
		Annotate.LOG.info("Static two returned " + MixinTarget.staticTwo("Hello, World!", 69));

		MixinTarget instance = new MixinTarget("Fuck you!");
		Annotate.LOG.info("Member one returned " + instance.memberOne(7729));

		// System.exit(0);

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
