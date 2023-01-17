package sh.emberj.annotate.registry;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateMod;
import sh.emberj.annotate.core.BaseAnnotation;
import sh.emberj.annotate.core.IMetaAnnotationType;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadata;

public class RegisterMetaAnnotationType implements IMetaAnnotationType {
    // @Override
    // public void handle(AnnotatedType type) throws AnnotateException {
    // RegisterMetaAnnotation[] annotations = tryGetAnnotations(type,
    // RegisterMetaAnnotation.class);
    // for (RegisterMetaAnnotation annotation : annotations) {
    // RegisterTypeHandler.addInfo(new RegisterAnnotationInfo(type.getAsClass(),
    // annotation.registry(),
    // annotation.loadStage()));
    // }
    // }

    @Override
    public BaseAnnotation createBaseAnnotation(AnnotationMetadata instance, ClassMetadata class_, AnnotateMod mod)
            throws AnnotateException {
        return new RegisterBaseAnnotation(instance, class_, mod);
    }
}
