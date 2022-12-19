package sh.emberj.annotate.core;

import java.lang.annotation.Annotation;

import com.google.common.reflect.TypeToken;

public abstract class AnnotatedTypeHandler {

    private final LoadStage _EXECUTION_STAGE;
    private final int _PRIORITY;

    protected AnnotatedTypeHandler(LoadStage executionStage, int priority) {
        _EXECUTION_STAGE = executionStage;
        _PRIORITY = priority;
    }

    protected AnnotatedTypeHandler(LoadStage executionStage) {
        this(executionStage, 0);
    }

    protected AnnotatedTypeHandler() {
        this(LoadStage.INIT, 0);
    }

    public LoadStage getExecutionStage() {
        return _EXECUTION_STAGE;
    }

    public int getExecutionPriority() {
        return _PRIORITY;
    }

    public void preHandle() throws AnnotateException { }

    public abstract void handle(AnnotatedType type) throws AnnotateException;

    public void postHandle() throws AnnotateException { }

    // vvv Static Helper Methods vvv

    protected static <T extends Annotation> T tryGetAnnotation(AnnotatedType type, Class<T> rawAnnotation)
            throws AnnotateException {
        return type.getAsClass().getAnnotation(rawAnnotation);
    }

    protected static <T extends Annotation> T[] tryGetAnnotations(AnnotatedType type, Class<T> rawAnnotation)
            throws AnnotateException {
        return type.getAsClass().getAnnotationsByType(rawAnnotation);
    }

    @SuppressWarnings("unchecked")
    protected static <T> T tryCastInstance(AnnotatedType type, TypeToken<T> expected) throws AnnotateException {
        return (T) tryCastInstance(type, expected.getRawType());
    }

    @SuppressWarnings("unchecked") // This is actually checked with 'clazz.isAssignableFrom'
    protected static <T> T tryCastInstance(AnnotatedType type, Class<T> expected) throws AnnotateException {
        if (!expected.isAssignableFrom(type.getAsClass())) return null;
        return (T) type.getInstance();
    }
}