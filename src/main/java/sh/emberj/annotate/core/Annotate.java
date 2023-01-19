package sh.emberj.annotate.core;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.impl.util.version.StringVersion;
import sh.emberj.annotate.core.asm.AnnotationContainer;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadata;
import sh.emberj.annotate.core.asm.ClassMetadataFactory;
import sh.emberj.annotate.core.asm.MethodMetadata;
import sh.emberj.annotate.core.tiny.TinyMapper;

public class Annotate {
    private Annotate() {
    }

    public static final Logger LOG = LoggerFactory.getLogger("Annotate");
    public static final String ID = "annotate";

    private static final List<AnnotateMod> _MODS;
    private static final Map<String, IMetaAnnotationType> _META_ANNOTATIONS;
    private static final Map<String, BaseAnnotation> _BASE_ANNOTATIONS;
    private static final Map<AnnotateLoadStage, List<ILoadListener>> _LOAD_LISTENERS;

    private static AnnotateLoadStage _loadStage;

    static {
        _MODS = new ArrayList<>();
        _META_ANNOTATIONS = new HashMap<>();
        _BASE_ANNOTATIONS = new HashMap<>();
        _LOAD_LISTENERS = new HashMap<>();
        try {

            for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
                AnnotateMod annotatedMod = AnnotateMod.tryCreate(mod);
                if (annotatedMod != null)
                    _MODS.add(annotatedMod);
            }

            {
                List<Pair<AnnotateMod, ClassMetadata>> modClasses = new ArrayList<>();

                for (AnnotateMod mod : _MODS) {
                    for (String package_ : mod.getPackages()) {
                        for (ClassMetadata class_ : ClassMetadataFactory.createAll(package_))
                            modClasses.add(Pair.of(mod, class_));
                    }
                }

                for (Pair<AnnotateMod, ClassMetadata> class_ : modClasses) {
                    AnnotationMetadata metaMetaAnnotationInstance = class_.getRight()
                            .getAnnotationByType(MetaMetaAnnotation.class);
                    if (metaMetaAnnotationInstance == null)
                        continue;
                    Type metaAnnotationTypeClass = metaMetaAnnotationInstance.getClassParam("value");
                    IMetaAnnotationType metaAnnotationType = (IMetaAnnotationType) Utils
                            .instantiate(metaAnnotationTypeClass);
                    _META_ANNOTATIONS.put(class_.getRight().toString(), metaAnnotationType);
                }

                for (Pair<AnnotateMod, ClassMetadata> class_ : modClasses) {
                    for (AnnotationMetadata annotation : class_.getRight().getAnnotations()) {
                        IMetaAnnotationType metaAnnotation = _META_ANNOTATIONS.get(annotation.getType().toString());
                        if (metaAnnotation == null)
                            continue;
                        BaseAnnotation base = metaAnnotation.createBaseAnnotation(annotation, class_.getRight(),
                                class_.getLeft());
                        _BASE_ANNOTATIONS.put(class_.getRight().toString(), base);
                        ClassMetadata repeatableContainer = base.getRepeatableContainer();
                        if (repeatableContainer != null)
                            _BASE_ANNOTATIONS.put(repeatableContainer.toString(),
                                    new RepeatableBaseAnnotation(annotation, repeatableContainer, class_.getLeft(),
                                            base));
                    }
                }

                for (Pair<AnnotateMod, ClassMetadata> class_ : modClasses) {
                    if (!checkEnvironment(class_.getRight()))
                        continue;
                    for (MethodMetadata method : class_.getRight().getMethods()) {
                        if (!checkEnvironment(method))
                            continue;
                        AnnotatedMethod annotatedMethod = null;
                        for (AnnotationMetadata annotation : method.getAnnotations()) {
                            BaseAnnotation annotationType = _BASE_ANNOTATIONS
                                    .get(annotation.getType().toString());
                            if (annotationType == null)
                                continue;
                            if (annotatedMethod == null)
                                annotatedMethod = new AnnotatedMethod(class_.getLeft(), method);
                            annotationType.handleInstance(annotation, annotatedMethod);
                        }
                    }

                    AnnotatedClass annotatedClass = null;
                    for (AnnotationMetadata annotation : class_.getRight().getAnnotations()) {
                        BaseAnnotation annotationType = _BASE_ANNOTATIONS
                                .get(annotation.getType().toString());
                        if (annotationType == null)
                            continue;
                        if (annotatedClass == null)
                            annotatedClass = new AnnotatedClass(class_.getLeft(), class_.getRight());
                        annotationType.handleInstance(annotation, annotatedClass);
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof AnnotateException ae)
                ae.showGUI();
            new AnnotateException("Error while initalizing Annotate.", e).showGUI();
        }
    }

    private static boolean checkEnvironment(AnnotationContainer container) throws AnnotateException {
        AnnotationMetadata envAnnotation = container.getAnnotationByType(Environment.class);
        if (envAnnotation == null)
            return true;
        EnvType env = envAnnotation.getEnumParam("value", EnvType.class);
        return env == FabricLoader.getInstance().getEnvironmentType();
    }

    public static File getDirectory() {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        File annotateFolder = new File(gameDir.toFile(), ".annotate");
        if (!annotateFolder.isDirectory())
            annotateFolder.mkdir();
        return annotateFolder;
    }

    private static Version _version;

    public static Version getVersion() {
        if (_version != null)
            return _version;
        _version = FabricLoader.getInstance().getModContainer(ID).get().getMetadata().getVersion();
        if (_version == null || _version.getFriendlyString().equals("${version}"))
            _version = new StringVersion("DEV");
        return _version;
    }

    private static TinyMapper _mapper;

    public static TinyMapper getTinyMapper() throws AnnotateException {
        if (_mapper != null)
            return _mapper;
        return _mapper = new TinyMapper();
    }

    public static String getBranding() {
        return "Annotate " + getVersion().getFriendlyString();
    }

    private static int listenerExecutionState = -1;

    static void updateLoadStage(AnnotateLoadStage loadStage) {
        try {
            int oldOrdinal = _loadStage == null ? -1 : _loadStage.ordinal();
            if (loadStage.ordinal() != oldOrdinal + 1)
                throw new AnnotateException("Invalid load stage set " + _loadStage + " -> " + loadStage + ".");

            LOG.info("Load stage " + loadStage);
            _loadStage = loadStage;
            List<ILoadListener> listeners = _LOAD_LISTENERS.get(_loadStage);
            if (listeners == null)
                return;

            for (listenerExecutionState = 0; listenerExecutionState < listeners.size(); listenerExecutionState++) {
                listeners.get(listenerExecutionState).onLoad();
            }
            listenerExecutionState = -1;
        } catch (Exception e) {
            if (e instanceof AnnotateException ae)
                ae.showGUI();
            else
                throw new RuntimeException(e);
            // new AnnotateException("Unknown error while loading Annotate.", e).showGUI();
        }
    }

    public static AnnotateLoadStage getLoadStage() {
        return _loadStage;
    }

    public static void addLoadListener(ILoadListener listener) {
        final AnnotateLoadStage listenerStage = listener.getLoadStage();
        if (_loadStage != null && _loadStage.ordinal() > listener.getLoadStage().ordinal()
                || (_loadStage == listener.getLoadStage() && listenerExecutionState == -1))
            throw new IllegalStateException("Cannot add listener on stage " + listenerStage
                    + " when already on load stage " + _loadStage + ".");
        List<ILoadListener> listeners = _LOAD_LISTENERS.computeIfAbsent(listenerStage, ls -> new ArrayList<>());

        int index = Collections.binarySearch(listeners, listener,
                Comparator.comparing(ILoadListener::getPriority).reversed());
        if (index < 0)
            index = -index - 1;
        else
            ++index;
        if (listenerStage == _loadStage && listenerExecutionState != -1 && index < listenerExecutionState)
            throw new IllegalStateException("Listener priority too high to be inserted. It should have already run.");
        listeners.add(index, listener);
    }
}
