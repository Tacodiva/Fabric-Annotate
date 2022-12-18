package sh.emberj.annotate.core;

import java.lang.annotation.Annotation;

import org.objectweb.asm.Type;

import sh.emberj.annotate.core.asm.AnnotationMeta;

public abstract class AnnotationHandler {

    public static record AnnotationInfo(AnnotationMeta annotation, AnnotatedType type) {
        public boolean isOfClass(Class<? extends Annotation> clazz) {
            return annotation.getType().equals(Type.getType(clazz));
        }
    }

    private final LoadStage _EXECUTION_STAGE;
    private final int _PRIORITY;

    public AnnotationHandler(LoadStage stage, int priority) {
        _EXECUTION_STAGE = stage;
        _PRIORITY = priority;
    }

    public LoadStage getExecutionStage() {
        return _EXECUTION_STAGE;
    }

    public int getExecutionPriority() {
        return _PRIORITY;
    }

    public void preHandle() throws AnnotateException {
    }

    public abstract void handle(AnnotationInfo annotation) throws AnnotateException;

    public void postHandle() throws AnnotateException {
    }

}
