package sh.emberj.annotate.core;

import java.lang.annotation.Repeatable;

import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadata;
import sh.emberj.annotate.core.asm.ClassMetadataFactory;

public abstract class BaseAnnotation {

    private final AnnotationMetadata _METADATA;
    private final AnnotateMod _MOD;
    private final ClassMetadata _CLASS;

    public BaseAnnotation(AnnotationMetadata metadata, ClassMetadata class_, AnnotateMod mod) {
        _METADATA = metadata;
        _MOD = mod;
        _CLASS = class_;
    }

    public abstract void handleInstance(AnnotationMetadata instance, AnnotatedClass annotatedClass)
            throws AnnotateException;

    public abstract void handleInstance(AnnotationMetadata instance, AnnotatedMethod annotatedMethod)
            throws AnnotateException;

    public AnnotationMetadata getMetadata() {
        return _METADATA;
    }

    public AnnotateMod getMod() {
        return _MOD;
    }

    public ClassMetadata getRepeatableContainer() throws AnnotateException {
        AnnotationMetadata repeatable = _CLASS.getAnnotationByType(Repeatable.class);
        if (repeatable == null) return null;
        return ClassMetadataFactory.create(repeatable.getClassParam("value"));
    }
}
