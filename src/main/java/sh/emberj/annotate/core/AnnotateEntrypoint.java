package sh.emberj.annotate.core;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceClassVisitor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.service.IClassTracker;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.include.com.google.common.io.Files;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.minecraft.client.gui.screen.TitleScreen;
import sh.emberj.annotate.mixin.AnnotateMixins;

public class AnnotateEntrypoint implements ModInitializer, PreLaunchEntrypoint {

	private static AnnotateEntrypoint _instance;

	public static AnnotateEntrypoint getInstance() {
		return _instance;
	}

	public static class Helloworld extends ClassLoader implements Opcodes {

		public static void main(final String args[]) throws Exception {

			// Generates the bytecode corresponding to the following Java class:
			//
			// public class Example {
			// public static void main (String[] args) {
			// System.out.println("Hello world!");
			// }
			// }

			// creates a ClassWriter for the Example public class,
			// which inherits from Object
			ClassWriter cw = new ClassWriter(0);


			AnnotationVisitor av = cw.visitAnnotation(Type.getDescriptor(Mixin.class), false);
			AnnotationVisitor avArr = av.visitArray("value");
			avArr.visit(null, Type.getType("Lnet/minecraft/client/gui/screen/TitleScreen;"));
			avArr.visitEnd();
			av.visitEnd();

			cw.visit(V1_1, ACC_PUBLIC | ACC_SUPER, "gen/omg/RuntimeGeneratedMixin", null, "java/lang/Object", null);

			// creates a MethodWriter for the (implicit) constructor
			MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);

			// pushes the 'this' variable
			mw.visitVarInsn(ALOAD, 0);
			// invokes the super class constructor
			mw.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mw.visitInsn(RETURN);
			// this code uses a maximum of one stack element and one local variable
			mw.visitMaxs(1, 1);
			mw.visitEnd();

			// creates a MethodWriter for the 'main' method
			mw = cw.visitMethod(ACC_PUBLIC, "testInject",
					"(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V", null, null);

			av = mw.visitAnnotation(Type.getDescriptor(Inject.class), true);
			avArr = av.visitArray("at");
			AnnotationVisitor subAv = avArr.visitAnnotation(null, Type.getDescriptor(At.class));
			subAv.visit("value", "HEAD");
			subAv.visitEnd();
			avArr.visitEnd();
			avArr = av.visitArray("method");
			avArr.visit(null, "init()V");
			avArr.visitEnd();
			av.visitEnd();

			// pushes the 'out' field (of type PrintStream) of the System class
			mw.visitFieldInsn(GETSTATIC, "sh/emberj/annotate/core/Annotate", "LOG", "Lorg/slf4j/Logger;");
			// pushes the "Hello World!" String constant
			mw.visitLdcInsn("OMG WE DID IT HYPE! (!!)");
			// invokes the 'println' method (defined in the PrintStream class)
			mw.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "info", "(Ljava/lang/String;)V", true);
			mw.visitInsn(RETURN);
			// this code uses a maximum of two stack elements and two local
			// variables
			mw.visitMaxs(2, 2);
			mw.visitEnd();

			// gets the bytecode of the Example class, and loads it dynamically
			byte[] code = cw.toByteArray();

			new File("gen").mkdir();

			File resourcesDir = new File("lmao");
			File output = new File("lmao/gen/omg/RuntimeGeneratedMixin.class");
			Files.write(code, output);

			// Helloworld loader = new Helloworld();
			// Class<?> exampleClass = loader.defineClass("RuntimeGeneratedMixin", code, 0,
			// code.length);

			// uses the dynamically generated class to print 'Helloworld'

			ClassReader reader = new ClassReader(code);
			TraceClassVisitor tcv = new TraceClassVisitor(new PrintWriter(System.out));
			reader.accept(tcv, 0);

			// FabricLoader.getInstance().

			// final URL classURL = output.toURI().toURL();

			// final String classB64 = Base64.getEncoder().encodeToString(code);
			// final URL classURL = new URL("data:application;base64," + classB64);

			// This is actually an instance of KnotClassLoader.DynamicURLClassLoader
			ClassLoader fabricRootClassLoader = FabricLauncherBase.getLauncher().getTargetClassLoader().getParent();

			try {
				Class<?> dynamicURLClassLoader = Class
						.forName("net.fabricmc.loader.impl.launch.knot.KnotClassLoader$DynamicURLClassLoader");
				Method method = dynamicURLClassLoader.getDeclaredMethod("addURL", new Class[] { URL.class });
				method.setAccessible(true);
				System.out.println(resourcesDir.toURI().toURL());
				method.invoke(fabricRootClassLoader, new Object[] { resourcesDir.toURI().toURL() });
				// method.invoke(fabricRootClassLoader, new Object[] { new File("test.json").toURI().toURL() });
			} catch (Throwable t) {
				t.printStackTrace();
			}

			Mixins.addConfiguration("test.json");

			Annotate.LOG.info("Added class to loader!");

			try {
				Method m = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
				m.setAccessible(true);
				m.invoke(null, MixinEnvironment.Phase.INIT);
				m.invoke(null, MixinEnvironment.Phase.DEFAULT);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			// exampleClass.getMethods()[0].invoke(null, new Object[] { null });
			// Annotate.LOG.info(exampleClass.getMethods()[0].toString());
			// Annotate.LOG.info(exampleClass.getMethods()[0].getAnnotationsByType(Blah.class).toString());
		}
	}

	@Override
	public void onPreLaunch() {
		// try {
		// 	ClassReader reader = new ClassReader("sh.emberj.annotate.core.mixin.TestInject");
		// 	// // StringWriter sw = new StringWriter();
		// 	TraceClassVisitor tcv = new TraceClassVisitor(new PrintWriter(System.out));
		// 	reader.accept(tcv, 0);
		// 	System.out.println("\n=============\n");
			// Helloworld.main(null);
		// } catch (Exception e) {
		// 	e.printStackTrace();
		// }
		_instance = this;
		Annotate.setLoadStage(LoadStage.PRELAUNCH);
		try {
			AnnotateMixins.runMixins();
		} catch (AnnotateException e) {
			throw new RuntimeException(e);
		}
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
		// System.exit(0);
	}
}
