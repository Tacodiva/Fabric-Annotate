package sh.emberj.annotate.core.handled;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedClass;
import sh.emberj.annotate.core.asm.AnnotationMetadata;

public interface IClassAnnotationHandler {
    public void handleClassAnnotation(AnnotatedClass class_, AnnotationMetadata annotation) throws AnnotateException;
}
