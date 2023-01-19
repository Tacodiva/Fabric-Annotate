package sh.emberj.annotate.core;

import sh.emberj.annotate.core.asm.AnnotationArrayMetadata;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadata;

class RepeatableBaseAnnotation extends BaseAnnotation {

    private final BaseAnnotation _SINGLE_TYPE;

    public RepeatableBaseAnnotation(ClassMetadata class_, AnnotateMod mod, BaseAnnotation singleType) {
        super(class_, mod);
        _SINGLE_TYPE = singleType;
    }

    @Override
    public void handleInstance(AnnotationMetadata instance, AnnotatedClass annotatedClass) throws AnnotateException {
        AnnotationArrayMetadata values = instance.getArrayParam("value");
        for (int i = 0; i < values.size(); i++)
            _SINGLE_TYPE.handleInstance(values.getAnnotation(i), annotatedClass);
    }

    @Override
    public void handleInstance(AnnotationMetadata instance, AnnotatedMethod annotatedMethod) throws AnnotateException {
        AnnotationArrayMetadata values = instance.getArrayParam("value");
        for (int i = 0; i < values.size(); i++)
            _SINGLE_TYPE.handleInstance(values.getAnnotation(i), annotatedMethod);
    }

}
