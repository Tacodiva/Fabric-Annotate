package sh.emberj.annotate.core;

import java.lang.annotation.Annotation;

public abstract class AnnotatedMethodHandler {

    private final LoadStage _EXECUTION_STAGE;

    protected AnnotatedMethodHandler(LoadStage executionStage) {
        _EXECUTION_STAGE = executionStage;
    }

    public LoadStage getExecutionStage() {
        return _EXECUTION_STAGE;
    }

    public abstract void handle(AnnotatedMethod method) throws AnnotateException;

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
