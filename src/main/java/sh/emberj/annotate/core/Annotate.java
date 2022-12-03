package sh.emberj.annotate.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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

public class Annotate {
    public static final Logger LOG = LoggerFactory.getLogger(Annotate.class);
    public static final String ID = "annotate";

    private static Annotate _instance;

    public static Annotate getInstance() {
        if (_instance == null) try {
            new Annotate();
        } catch (AnnotateException e) {
            throw e.rethrow();
        }
        return _instance;
    }

    public static LoadStage getLoadStage() {
        if (_instance == null) return null;
        return _instance._loadStage;
    }

    static void setLoadStage(LoadStage loadStage) {
        Annotate inst = getInstance();
        if (inst._loadStage != loadStage) {
            LOG.info("Load stage " + loadStage);
            inst._loadStage = loadStage;
            inst.executeHandlers(loadStage);
        }
    }

    private final Reflections _REFLECTIONS;
    private final Set<AnnotatedMod> _MODS;
    private final Set<AnnotatedType> _TYPES;
    private final Set<AnnotatedMethod> _METHODS;

    private final Map<LoadStage, Set<AnnotatedTypeHandler>> _TYPE_HANDLERS;
    private final Map<LoadStage, Set<AnnotatedMethodHandler>> _METHOD_HANDLERS;

    private LoadStage _loadStage;

    private Annotate() throws AnnotateException {
        _instance = this;

        // Step 1. Find all the mods with annotate packages
        _MODS = new HashSet<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            AnnotatedMod annotatedMod = AnnotatedMod.tryCreate(mod);
            if (annotatedMod != null) _MODS.add(annotatedMod);
        }
        URL[] packageURLs = _MODS.stream()
                .flatMap(mod -> Arrays.stream(mod.getPackages()).flatMap(p -> ClasspathHelper.forPackage(p).stream()))
                .toArray(URL[]::new);

        LOG.info("Starting Annotate for " + _MODS.size() + " mod(s).");

        // Step 2. Enumerate all the classes in those packages with Reflections
        Reflections.log.info("Starting reflections scan on " + packageURLs.length + " packages.");
        _REFLECTIONS = new Reflections(new ConfigurationBuilder().setUrls(packageURLs)
                .setScanners(Scanners.TypesAnnotated, Scanners.MethodsAnnotated));

        // Step 3. Search for all the annotations that we care about
        // All annotations that we need to something with are themselves annotated with
        // @AnnotateAnnotation
        Set<Class<?>> annotations = _REFLECTIONS.getTypesAnnotatedWith(AnnotateAnnotation.class, true);

        // Step 4. Find all the classes and functions annotated with one of the
        // annotations found in the above step.
        Set<Class<?>> classesToScan = new HashSet<>();
        Set<Method> methodsToScan = new HashSet<>();
        for (Class<?> annotationClass : annotations) {
            // This is safe because ScannableAnnotation can only be put on annotations
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> annotation = (Class<? extends Annotation>) annotationClass;

            classesToScan.addAll(_REFLECTIONS.getTypesAnnotatedWith(annotation, false));
            methodsToScan.addAll(_REFLECTIONS.getMethodsAnnotatedWith(annotation));
        }
        // Convert them into instances of AnnotatedType
        _TYPES = classesToScan.stream().map(AnnotatedType::new).collect(Collectors.toSet());
        _METHODS = methodsToScan.stream().map(AnnotatedMethod::new).collect(Collectors.toSet());

        LOG.info("Found " + _TYPES.size() + " annotated types and " + methodsToScan.size() + " methods using "
                + annotations.size() + " annotations.");

        // Step 5. Find all the type and method handlers and store them so they can be
        // run later
        _TYPE_HANDLERS = new HashMap<>();
        _METHOD_HANDLERS = new HashMap<>();
        executeTypeHandler(new HandlerHandler());
    }

    AnnotatedMod findModFromPackage(String packageName) {
        for (AnnotatedMod mod : _MODS) {
            if (mod.containsPackage(packageName)) return mod;
        }
        throw new RuntimeException("Could not find mod containing the package '" + packageName + "'.");
    }

    private void executeHandlers(LoadStage stage) {
        try {
            Set<AnnotatedTypeHandler> typeHandlers = _TYPE_HANDLERS.get(stage);
            if (typeHandlers != null) {
                for (AnnotatedTypeHandler handler : typeHandlers)
                    executeTypeHandler(handler);
            }
            Set<AnnotatedMethodHandler> methodHandlers = _METHOD_HANDLERS.get(stage);
            if (methodHandlers != null) {
                for (AnnotatedMethodHandler handler : methodHandlers)
                    executeMethodHandler(handler);
            }
        } catch (AnnotateException e) {
            throw e.rethrow();
        }
        if (stage != null) executeHandlers(null);
    }

    private void executeMethodHandler(AnnotatedMethodHandler handler) throws AnnotateException {
        LOG.debug("Running method handler " + handler);
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

    private class HandlerHandler extends AnnotatedTypeHandler {
        public HandlerHandler() {
            super(null);
        }

        @Override
        public void handle(AnnotatedType type) throws AnnotateException {
            tryAddTypeHandler(type);
            tryAddMethodHandler(type);
        }

        private void tryAddTypeHandler(AnnotatedType type) throws AnnotateException {
            AnnotatedTypeHandler handler = tryCastInstance(type, AnnotatedTypeHandler.class);
            if (handler == null) return;
            LOG.info("Found " + handler.getExecutionStage() + " stage type handler " + handler);
            Set<AnnotatedTypeHandler> handlers = _TYPE_HANDLERS.get(handler.getExecutionStage());
            if (handlers == null) {
                handlers = new HashSet<>();
                _TYPE_HANDLERS.put(handler.getExecutionStage(), handlers);
            }
            handlers.add(handler);
        }

        private void tryAddMethodHandler(AnnotatedType type) throws AnnotateException {
            AnnotatedMethodHandler handler = tryCastInstance(type, AnnotatedMethodHandler.class);
            if (handler == null) return;
            LOG.info("Found " + handler.getExecutionStage() + " stage method handler " + handler);
            Set<AnnotatedMethodHandler> handlers = _METHOD_HANDLERS.get(handler.getExecutionStage());
            if (handlers == null) {
                handlers = new HashSet<>();
                _METHOD_HANDLERS.put(handler.getExecutionStage(), handlers);
            }
            handlers.add(handler);
        }
    }
}