package sh.emberj.annotate.core;

import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadata;

public interface IMetaAnnotationType {

    public BaseAnnotation createBaseAnnotation(AnnotationMetadata instance, ClassMetadata class_, AnnotateMod mod)
            throws AnnotateException;
}
