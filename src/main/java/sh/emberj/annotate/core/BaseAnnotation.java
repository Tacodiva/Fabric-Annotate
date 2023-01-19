package sh.emberj.annotate.core;

import java.lang.annotation.Repeatable;

import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadata;
import sh.emberj.annotate.core.asm.ClassMetadataFactory;

public abstract class BaseAnnotation {

    private final AnnotateMod _MOD;
    private final ClassMetadata _ANNOTATION;

    public BaseAnnotation(ClassMetadata annotation, AnnotateMod mod) {
        _MOD = mod;
        _ANNOTATION = annotation;
    }

    public abstract void handleInstance(AnnotationMetadata instance, AnnotatedClass annotatedClass)
            throws AnnotateException;

    public abstract void handleInstance(AnnotationMetadata instance, AnnotatedMethod annotatedMethod)
            throws AnnotateException;

    public AnnotateMod getMod() {
        return _MOD;
    }

    public ClassMetadata getAnnotation() {
        return _ANNOTATION;
    }

    public ClassMetadata getRepeatableContainer() throws AnnotateException {
        AnnotationMetadata repeatable = _ANNOTATION.getAnnotationByType(Repeatable.class);
        if (repeatable == null) return null;
        return ClassMetadataFactory.create(repeatable.getClassParam("value"));
    }
}
