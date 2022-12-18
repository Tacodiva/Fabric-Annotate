package sh.emberj.annotate.mixin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.MixinEnvironment.Option;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.include.com.google.common.io.Files;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.launch.knot.Knot;
import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.Utils;
import sh.emberj.annotate.mixin.asm.DynamicMixinClass;
import sh.emberj.annotate.mixin.asm.IDynamicMixinMethodGenerator;

public class AnnotateMixins {

    private AnnotateMixins() {
    }

    private static final String MIXINS_JSON_NAME = "annotate.codegen.mixins.json";
    private static final String MIXINS_PACKAGE_NAME = "sh.emberj.annotate.generated";
    private static final String MIXINS_PACKAGE_INTERNAL = MIXINS_PACKAGE_NAME.replace('.', '/');

    private static Map<String, DynamicMixinClass> _classes = new HashMap<>();

    private static void checkHaventRun() throws AnnotateException {
        if (_classes == null)
            throw new AnnotateException("Already ran the mixins!");
    }

    public static void addMixin(IDynamicMixinMethodGenerator mixin) throws AnnotateException {
        checkHaventRun();
        DynamicMixinClass clazz = getMixinClass(mixin.getTargetType());
        clazz.addMethod(mixin);
    }

    public static DynamicMixinClass getMixinClass(Type target) throws AnnotateException {
        checkHaventRun();
        DynamicMixinClass clazz = _classes.get(target.toString());
        if (clazz == null) {
            clazz = new DynamicMixinClass(target, MIXINS_PACKAGE_INTERNAL + "/" + generateClassName(target));
            _classes.put(target.toString(), clazz);
        }
        return clazz;
    }

    public static void runMixins() throws AnnotateException {
        checkHaventRun();
        long startTime = System.currentTimeMillis();
        if (_classes.isEmpty())
            return;
        File codegen;
        try {
            File annotateFolder = Annotate.getAnnotateDirectory();
            codegen = new File(annotateFolder, "codegen");
            FileUtils.deleteDirectory(codegen);

            codegen.mkdir();

            File codePackage = new File(codegen, MIXINS_PACKAGE_INTERNAL);
            codePackage.mkdirs();

            {
                JsonObject mixinsJson = new JsonObject();

                mixinsJson.addProperty("required", true);
                mixinsJson.addProperty("minVersion", "0.8");
                mixinsJson.addProperty("package", MIXINS_PACKAGE_NAME);
                mixinsJson.addProperty("compatibilityLevel", "JAVA_17");

                JsonArray mixins = new JsonArray();
                for (DynamicMixinClass clazz : _classes.values()) {
                    mixins.add(getSimpleClassName(getSimpleClassName(clazz.getClassName())));
                }
                mixinsJson.add("mixins", mixins);

                JsonObject injectors = new JsonObject();
                injectors.addProperty("defaultRequire", 1);
                mixinsJson.add("injectors", injectors);

                String contents = mixinsJson.toString();
                contents = "// Automatically generated by " + Annotate.getBranding() + "\n" + contents;

                Files.write(contents.getBytes(), new File(codegen, MIXINS_JSON_NAME));
            }

            for (DynamicMixinClass clazz : _classes.values()) {
                byte[] bytecode = clazz.generateBytecode();
                String className = getSimpleClassName(clazz.getClassName());
                Files.write(bytecode, new File(codePackage, className + ".class"));
                Annotate.LOG.info("Codegen: Wrote " + className);
            }
        } catch (IOException e) {
            throw new AnnotateException("Error while writing generated files to disk.", e);
        }

        Utils.injectClasspathFile(codegen);

        Annotate.LOG.info("Codegen: Finished writing " + _classes.size() + " classes in "
                + (System.currentTimeMillis() - startTime) + " ms");
        _classes = null;

        // Force the mixin processor to load our new configuration.
        // This is probably the most horrible thing I have ever done.
        try {
            // Add the mixins to the current enviroment
            Mixins.addConfiguration(MIXINS_JSON_NAME);
            // Enable logging so we can see the mixins being applied
            MixinEnvironment.getCurrentEnvironment().setOption(Option.DEBUG_VERBOSE, true);

            // Get Knot.classLoader
            // https://github.com/FabricMC/fabric-loader/blob/354af34127e52378410d182dd7b458e8c9b893d5/src/main/java/net/fabricmc/loader/impl/launch/knot/Knot.java#L56
            Field knotClassLoader = Knot.class.getDeclaredField("classLoader");
            knotClassLoader.setAccessible(true);
            Object kcd = knotClassLoader.get(FabricLauncherBase.getLauncher());
            Class<?> kcdClass = kcd.getClass(); // KnotClassDelegate

            // Get KnotClassDelegate.mixinTransformer
            // https://github.com/FabricMC/fabric-loader/blob/354af34127e52378410d182dd7b458e8c9b893d5/src/main/java/net/fabricmc/loader/impl/launch/knot/KnotClassDelegate.java#L83
            Field kcdMixinTransformer = kcdClass.getDeclaredField("mixinTransformer");
            kcdMixinTransformer.setAccessible(true);
            IMixinTransformer transformer = (IMixinTransformer) kcdMixinTransformer.get(kcd);
            Class<?> transformerClass = transformer.getClass(); // MixinTransformer

            // Get MixinTransformer.processor
            // https://github.com/SpongePowered/Mixin/blob/155314e6e91465dad727e621a569906a410cd6f4/src/main/java/org/spongepowered/asm/mixin/transformer/MixinTransformer.java#L89
            Field transformerProcessor = transformerClass.getDeclaredField("processor");
            transformerProcessor.setAccessible(true);
            Object processor = transformerProcessor.get(transformer);
            Class<?> processorClass = processor.getClass(); // MixinProcessor

            // Get and invoke MixinProcessor.select
            // https://github.com/SpongePowered/Mixin/blob/155314e6e91465dad727e621a569906a410cd6f4/src/main/java/org/spongepowered/asm/mixin/transformer/MixinProcessor.java#L448
            Method processorSelect = processorClass.getDeclaredMethod("select", MixinEnvironment.class);
            processorSelect.setAccessible(true);
            processorSelect.invoke(processor, MixinEnvironment.getCurrentEnvironment());

            MixinEnvironment.getCurrentEnvironment().setOption(Option.DEBUG_VERBOSE, false);
        } catch (Exception e) {
            throw new AnnotateException("Encountered exception while force loading mixins.", e);
        }
    }

    private static int classUniquifier = 0;

    private static String generateClassName(Type target) {
        String mainClassBit = getSimpleClassName(target.getClassName());
        mainClassBit = mainClassBit.replace('+', '$').replace('-', '$');
        return mainClassBit + "$" + (classUniquifier++);
    }

    private static String getSimpleClassName(String fullName) {
        String[] classBits = fullName.split("\\.");
        classBits = classBits[classBits.length - 1].split("/");
        return classBits[classBits.length - 1];
    }

}
