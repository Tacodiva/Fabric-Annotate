package sh.emberj.annotate.core;

import java.lang.annotation.Annotation;

import com.google.common.reflect.TypeToken;

public abstract class ThatThingThatsUnused<TAnnotation extends Annotation, TClass> extends AnnotatedTypeHandler {

    protected static enum TypeRequirements {
        CLASS(true, false), 
        ANNOTATION(false, true), 
        BOTH(true, true),
        EITHER(false, false);

        public final boolean classRequired, annotationRequired;

        private TypeRequirements(boolean classRequired, boolean annotationRequired) {
            this.classRequired = classRequired;
            this.annotationRequired = annotationRequired;
        }
    }

    private final Class<TAnnotation> _RAW_ANNOTATION;
    private final TypeToken<TClass> _TYPE;
    private final TypeRequirements _REQUIREMENTS;

    protected ThatThingThatsUnused(LoadStage executionStage, TypeRequirements requirements,
            Class<TAnnotation> annotation, Class<TClass> clazz) {
        this(executionStage, requirements, annotation, TypeToken.of(clazz));
    }

    protected ThatThingThatsUnused(LoadStage executionStage, TypeRequirements requirements,
            Class<TAnnotation> annotation, TypeToken<TClass> clazz) {
        super(executionStage);
        _RAW_ANNOTATION = annotation;
        _TYPE = clazz;
        _REQUIREMENTS = requirements;
    }

    @Override
    public void handle(AnnotatedType type) throws AnnotateException {
        TClass instance = tryCastInstance(type, _TYPE);
        TAnnotation annotation = tryGetAnnotation(type, _RAW_ANNOTATION);

        if (instance == null && annotation == null) return;

        if (instance == null && _REQUIREMENTS.classRequired) throw new AnnotateException(
                "Types annotated with '@" + _RAW_ANNOTATION + "' must inherit from '" + _TYPE + "'.", type);
        if (annotation == null && _REQUIREMENTS.annotationRequired) throw new AnnotateException(
                "Types inheriting from '" + _TYPE + "' must be annotated with '@" + _RAW_ANNOTATION + "'.", type);

        handle(type, instance, annotation);
    }

    protected abstract void handle(AnnotatedType type, TClass instance, TAnnotation annotation)
            throws AnnotateException;
}