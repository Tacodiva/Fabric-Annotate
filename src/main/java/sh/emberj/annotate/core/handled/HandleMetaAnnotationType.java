package sh.emberj.annotate.core.handled;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateMod;
import sh.emberj.annotate.core.IMetaAnnotationType;
import sh.emberj.annotate.core.BaseAnnotation;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadata;

public class HandleMetaAnnotationType implements IMetaAnnotationType {

    @Override
    public BaseAnnotation createBaseAnnotation(AnnotationMetadata annotation, ClassMetadata class_, AnnotateMod mod)
            throws AnnotateException {
        return new HandledBaseAnnotation(annotation, class_, mod);
    }

}
