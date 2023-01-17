package sh.emberj.annotate.core.handled;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.asm.AnnotationMetadata;

public interface IMethodAnnotationHandler {
    public void handleMethodAnnotation(AnnotatedMethod method, AnnotationMetadata annotation) throws AnnotateException;
}
