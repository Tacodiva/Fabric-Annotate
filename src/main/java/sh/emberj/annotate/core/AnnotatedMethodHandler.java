package sh.emberj.annotate.core;

import java.io.IOException;
import java.lang.annotation.Annotation;

import org.objectweb.asm.ClassReader;

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

    protected static void tryGetUnloadedAnnotation(AnnotatedMethod method) {
        try {
            ClassReader cr = new ClassReader(method.getClass().getCanonicalName());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    protected static <T extends Annotation> T tryGetAnnotation(AnnotatedMethod method, Class<T> rawAnnotation)
            throws AnnotateException {
        return method.getMethod().getAnnotation(rawAnnotation);
    }

    protected static <T extends Annotation> T[] tryGetAnnotations(AnnotatedMethod method, Class<T> rawAnnotation)
            throws AnnotateException {
        return method.getMethod().getAnnotationsByType(rawAnnotation);
    }

}
