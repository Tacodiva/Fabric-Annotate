package sh.emberj.annotate.core;

import java.lang.annotation.Annotation;

import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadata;

public interface IMetaAnnotationType {

    public BaseAnnotation createBaseAnnotation(AnnotationMetadata instance, ClassMetadata class_, AnnotateMod mod)
            throws AnnotateException;

    public Class<? extends Annotation> getAnnotation();
}
