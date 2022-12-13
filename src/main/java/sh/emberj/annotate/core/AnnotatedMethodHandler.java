package sh.emberj.annotate.core;

import java.lang.annotation.Annotation;

public abstract class AnnotatedMethodHandler {

    private final LoadStage _EXECUTION_STAGE;
    private final int _PRIORITY;

    protected AnnotatedMethodHandler(LoadStage executionStage, int priority) {
        _EXECUTION_STAGE = executionStage;
        _PRIORITY = priority;
    }

    protected AnnotatedMethodHandler(LoadStage executionStage) {
        this(executionStage, 0);
    }

    protected AnnotatedMethodHandler() {
        this(LoadStage.INIT, 0);
    }

    public LoadStage getExecutionStage() {
        return _EXECUTION_STAGE;
    }

    public int getExecutionPriority() {
        return _PRIORITY;
    }
    
    public void preHandle() throws AnnotateException { }

    public abstract void handle(AnnotatedMethod method) throws AnnotateException;

    public void postHandle() throws AnnotateException { }
    
    // vvv Static Helper Methods vvv

    protected static <T extends Annotation> T tryGetAnnotation(AnnotatedMethod method, Class<T> rawAnnotation)
            throws AnnotateException {
        return method.getMethod().getAnnotation(rawAnnotation);
    }

    protected static <T extends Annotation> T[] tryGetAnnotations(AnnotatedMethod method, Class<T> rawAnnotation)
            throws AnnotateException {
        return method.getMethod().getAnnotationsByType(rawAnnotation);
    }

}
