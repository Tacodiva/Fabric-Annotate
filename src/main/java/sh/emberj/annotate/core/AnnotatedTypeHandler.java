package sh.emberj.annotate.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

import com.google.common.reflect.TypeToken;

public abstract class AnnotatedTypeHandler {

    private final LoadStage _EXECUTION_STAGE;

    protected AnnotatedTypeHandler(LoadStage executionStage) {
        _EXECUTION_STAGE = executionStage;
    }

    public LoadStage getExecutionStage() {
        return _EXECUTION_STAGE;
    }

    public void preHandle() throws AnnotateException { }

    public abstract void handle(AnnotatedType type) throws AnnotateException;

    public void postHandle() throws AnnotateException { }

    // vvv Static Helper Methods vvv

    protected static <T extends Annotation> T tryGetAnnotation(AnnotatedType type, Class<T> rawAnnotation)
            throws AnnotateException {
        return type.getRawType().getAnnotation(rawAnnotation);
    }

    protected static <T extends Annotation> T[] tryGetAnnotations(AnnotatedType type, Class<T> rawAnnotation)
            throws AnnotateException {
        return type.getRawType().getAnnotationsByType(rawAnnotation);
    }

    @SuppressWarnings("unchecked")
    protected static <T> T tryCastInstance(AnnotatedType type, TypeToken<T> expected) throws AnnotateException {
        return (T) tryCastInstance(type, expected.getRawType());
    }

    @SuppressWarnings("unchecked") // This is actually checked with 'clazz.isAssignableFrom'
    protected static <T> T tryCastInstance(AnnotatedType type, Class<T> expected) throws AnnotateException {
        if (!expected.isAssignableFrom(type.getRawType())) return null;
        Object instance = type.getInstance();
        if (instance != null) return (T) instance;
        return (T) createInstance(type);
    }

    protected static Object getInstance(AnnotatedType type) throws AnnotateException {
        Object instance = type.getInstance();
        if (instance != null) return instance;
        return createInstance(type);
    }

    protected static Object createInstance(AnnotatedType type) throws AnnotateException {
        try {
            Object inst = type.getRawType().getConstructor().newInstance();
            type.setInstance(inst);
            return inst;
        } catch (InvocationTargetException e) {
            throw new AnnotateException("Exception thrown by class constructor. ", type, e.getCause());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException
                | SecurityException e) {
            throw new AnnotateException(
                    "Exception while creating instance, make sure the type has a constructor which takes no arguments. "
                            + e.getMessage(),
                    type);
        }
    }
}