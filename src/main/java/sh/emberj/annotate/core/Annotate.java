package sh.emberj.annotate.core;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.impl.util.version.StringVersion;
import sh.emberj.annotate.core.AnnotationHandler.AnnotationInfo;
import sh.emberj.annotate.core.asm.AnnotatedMethodMeta;
import sh.emberj.annotate.core.asm.AnnotatedTypeMeta;
import sh.emberj.annotate.core.asm.AnnotationMeta;
import sh.emberj.annotate.core.mapping.AnoMapper;
import sh.emberj.annotate.entrypoint.EntrypointManager;

public class Annotate {
    public static final Logger LOG = LoggerFactory.getLogger(Annotate.class);
    public static final String ID = "annotate";

    private static Annotate _instance;
    private static AnoMapper _mapper;
    private static Version _version;

    public static Annotate getInstance() {
        if (_instance == null)
            try {
                new Annotate();
            } catch (AnnotateException e) {
                e.showGUI();
            }
        return _instance;
    }

    public static LoadStage getLoadStage() {
        if (_instance == null)
            return null;
        return _instance._loadStage;
    }

    static void setLoadStage(LoadStage loadStage) {
        Annotate inst = getInstance();
        if (inst._loadStage != loadStage) {
            LOG.info("Load stage " + loadStage);
            inst._loadStage = loadStage;
            inst.executeHandlers(loadStage);
            EntrypointManager.invokeEntrypoints(loadStage);
        }
    }

    public static File getAnnotateDirectory() {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        File annotateFolder = new File(gameDir.toFile(), ".annotate");
        if (!annotateFolder.isDirectory())
            annotateFolder.mkdir();
        return annotateFolder;
    }

    public static Version getVersion() {
        if (_version != null)
            return _version;
        _version = FabricLoader.getInstance().getModContainer(ID).get().getMetadata().getVersion();
        if (_version == null || _version.getFriendlyString().equals("${version}"))
            _version = new StringVersion("DEV");
        return _version;
    }

    public static String getBranding() {
        return "Annotate " + getVersion().getFriendlyString();
    }

    public static AnoMapper getMapper() {
        if (_mapper != null)
            return _mapper;
        try {
            long time = System.currentTimeMillis();
            int beforeSize = AnnotatedTypeMeta.getCacheSize();
            _mapper = new AnoMapper();
            LOG.info("Loaded " + (AnnotatedTypeMeta.getCacheSize() - beforeSize) + " classes and their mappings in "
                    + (System.currentTimeMillis() - time) + " ms");
            return _mapper;
        } catch (AnnotateException e) {
            e.showGUI();
            throw new AssertionError();
        }
    }

    private final Reflections _REFLECTIONS;
    private final Set<AnnotatedMod> _MODS;
    private final Set<AnnotatedType> _TYPES;
    private final Set<AnnotatedMethod> _METHODS;
    private final Set<AnnotationInfo> _ANNOTATIONS;

    private final Map<LoadStage, List<AnnotatedTypeHandler>> _TYPE_HANDLERS;
    private final Map<LoadStage, List<AnnotatedMethodHandler>> _METHOD_HANDLERS;
    private final Map<LoadStage, List<AnnotationHandler>> _ANNOTATION_HANDLERS;

    private LoadStage _loadStage;

    private Annotate() throws AnnotateException {
        _instance = this;
        LOG.info("Starting " + getBranding());

        // Step 1. Find all the mods with annotate packages
        _MODS = new HashSet<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            AnnotatedMod annotatedMod = AnnotatedMod.tryCreate(mod);
            if (annotatedMod != null)
                _MODS.add(annotatedMod);
        }
        URL[] packageURLs = _MODS.stream()
                .flatMap(mod -> Arrays.stream(mod.getPackages()).flatMap(p -> ClasspathHelper.forPackage(p).stream()))
                .toArray(URL[]::new);

        // _MODS.iterator().next().getResourceGenerator().generate(new TestResource());

        // Step 2. Enumerate all the classes in those packages with Reflections
        Reflections.log.info("Starting reflections scan on " + packageURLs.length + " packages.");
        _REFLECTIONS = new Reflections(new ConfigurationBuilder().setUrls(packageURLs)
                .setScanners(Scanners.TypesAnnotated, Scanners.MethodsAnnotated));

        // Step 3. Search for all the annotations that we care about
        // All annotations that we need to something with are themselves annotated with
        // @AnnotateAnnotation
        Set<Class<?>> annotations = _REFLECTIONS.getTypesAnnotatedWith(AnnotateAnnotation.class, true);

        _ANNOTATIONS = new HashSet<>();
        // Step 4. Find all the classes annotated with one of the annotations found in
        // the above step.
        _TYPES = new HashSet<>();
        // Set<Class<?>> classesToScan = new HashSet<>();
        for (Class<?> annotationClass : annotations) {
            // This is safe because ScannableAnnotation can only be put on annotations
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> annotation = (Class<? extends Annotation>) annotationClass;
            Set<Class<?>> annotatedClasses = _REFLECTIONS.getTypesAnnotatedWith(annotation, true);
            for (Class<?> annotatedClass : annotatedClasses) {
                AnnotatedType annotatedType = new AnnotatedType(annotatedClass);
                _TYPES.add(annotatedType);
                _ANNOTATIONS.add(
                        new AnnotationInfo(annotatedType.getMeta().getAnnotationByType(annotation), annotatedType));
            }
        }

        // Step 4.5. Find all the methods to scan
        Set<AnnotatedMethodMeta> methodsToScan = new HashSet<>();
        for (AnnotatedType type : _TYPES) {
            AnnotatedTypeMeta typeMeta = type.getMeta();

            for (AnnotatedMethodMeta method : typeMeta.getMethods()) {
                if (!method.hasAnnotations())
                    continue;
                for (Class<?> annotationClass : annotations) {
                    AnnotationMeta annotation = method.getAnnotationByType(annotationClass);
                    if (annotation != null) {
                        methodsToScan.add(method);
                        _ANNOTATIONS.add(new AnnotationInfo(annotation, type));
                        break;
                    }
                }
            }
        }
        _METHODS = methodsToScan.stream().map(AnnotatedMethod::new).collect(Collectors.toSet());

        LOG.info("Found " + _TYPES.size() + " annotated types and " + methodsToScan.size() + " methods using "
                + annotations.size() + " annotations.");

        // Step 5. Find all the type and method handlers and store them so they can be
        // run later
        _TYPE_HANDLERS = new HashMap<>();
        _METHOD_HANDLERS = new HashMap<>();
        _ANNOTATION_HANDLERS = new HashMap<>();
        executeTypeHandler(new HandlerHandler());
    }

    AnnotatedMod findModFromPackage(String packageName) {
        for (AnnotatedMod mod : _MODS) {
            if (mod.containsPackage(packageName))
                return mod;
        }
        throw new RuntimeException("Could not find mod containing the package '" + packageName + "'.");
    }

    private void executeHandlers(LoadStage stage) {
        try {
            List<AnnotatedTypeHandler> typeHandlers = _TYPE_HANDLERS.get(stage);
            if (typeHandlers != null) {
                for (AnnotatedTypeHandler handler : typeHandlers)
                    executeTypeHandler(handler);
            }
            List<AnnotatedMethodHandler> methodHandlers = _METHOD_HANDLERS.get(stage);
            if (methodHandlers != null) {
                for (AnnotatedMethodHandler handler : methodHandlers)
                    executeMethodHandler(handler);
            }
            List<AnnotationHandler> annotationHandlers = _ANNOTATION_HANDLERS.get(stage);
            if (annotationHandlers != null) {
                for (AnnotationHandler handler : annotationHandlers)
                    executeAnnotationHandler(handler);
            }
        } catch (AnnotateException e) {
            e.showGUI();
        }
        if (stage != null)
            executeHandlers(null);
    }

    private void executeMethodHandler(AnnotatedMethodHandler handler) throws AnnotateException {
        LOG.debug("Running method handler " + handler);
        handler.preHandle();
        for (AnnotatedMethod method : _METHODS) {
            try {
                handler.handle(method);
            } catch (AnnotateException e) {
                e.trySet(method);
                throw e;
            } catch (Exception e) {
                throw new AnnotateException("Unexpected error while handling method.", method, e);
            }
        }
        handler.postHandle();
    }

    private void executeTypeHandler(AnnotatedTypeHandler handler) throws AnnotateException {
        LOG.debug("Running type handler " + handler);
        handler.preHandle();
        for (AnnotatedType type : _TYPES) {
            try {
                handler.handle(type);
            } catch (AnnotateException e) {
                e.trySet(type);
                throw e;
            } catch (Exception e) {
                throw new AnnotateException("Unexpected error while handling type.", type, e);
            }
        }
        handler.postHandle();
    }

    private void executeAnnotationHandler(AnnotationHandler handler) throws AnnotateException {
        LOG.debug("Running annotation handler " + handler);
        handler.preHandle();
        for (AnnotationInfo annotation : _ANNOTATIONS) {
            try {
                handler.handle(annotation);
            } catch (AnnotateException e) {
                e.trySet(annotation.type());
                throw e;
            } catch (Exception e) {
                throw new AnnotateException("Unexpected error while handling type.", annotation.type(), e);
            }
        }
        handler.postHandle();
    }

    private class HandlerHandler extends AnnotatedTypeHandler {
        public HandlerHandler() {
            super(null);
        }

        @Override
        public void handle(AnnotatedType type) throws AnnotateException {
            tryAddTypeHandler(type);
            tryAddMethodHandler(type);
            tryAddAnnotationHandler(type);
        }

        private void tryAddTypeHandler(AnnotatedType type) throws AnnotateException {
            AnnotatedTypeHandler handler = tryCastInstance(type, AnnotatedTypeHandler.class);
            if (handler == null)
                return;
            LOG.info("Found " + handler.getExecutionStage() + " stage type handler " + handler);
            _TYPE_HANDLERS.computeIfAbsent(handler.getExecutionStage(), a -> new ArrayList<>()).add(handler);
        }

        private void tryAddMethodHandler(AnnotatedType type) throws AnnotateException {
            AnnotatedMethodHandler handler = tryCastInstance(type, AnnotatedMethodHandler.class);
            if (handler == null)
                return;
            LOG.info("Found " + handler.getExecutionStage() + " stage method handler " + handler);
            _METHOD_HANDLERS.computeIfAbsent(handler.getExecutionStage(), a -> new ArrayList<>()).add(handler);
        }

        private void tryAddAnnotationHandler(AnnotatedType type) throws AnnotateException {
            AnnotationHandler handler = tryCastInstance(type, AnnotationHandler.class);
            if (handler == null)
                return;
            LOG.info("Found " + handler.getExecutionStage() + " stage annotation handler " + handler);
            _ANNOTATION_HANDLERS.computeIfAbsent(handler.getExecutionStage(), a -> new ArrayList<>()).add(handler);
        }

        @Override
        public void postHandle() {
            for (List<AnnotatedTypeHandler> typeHandlers : _TYPE_HANDLERS.values())
                typeHandlers.sort((a, b) -> Integer.compare(a.getExecutionPriority(), b.getExecutionPriority()));
            for (List<AnnotatedMethodHandler> methodHandlers : _METHOD_HANDLERS.values())
                methodHandlers.sort((a, b) -> Integer.compare(a.getExecutionPriority(), b.getExecutionPriority()));
            for (List<AnnotationHandler> annotationHandlers : _ANNOTATION_HANDLERS.values())
                annotationHandlers.sort((a, b) -> Integer.compare(a.getExecutionPriority(), b.getExecutionPriority()));
        }
    }
}