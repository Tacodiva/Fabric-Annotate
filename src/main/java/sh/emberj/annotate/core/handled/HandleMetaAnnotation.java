package sh.emberj.annotate.core.handled;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.core.AnnotateMod;
import sh.emberj.annotate.core.AnnotationFactory;
import sh.emberj.annotate.core.BaseAnnotation;
import sh.emberj.annotate.core.IAnnotationFactory;
import sh.emberj.annotate.core.IMetaAnnotationType;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadata;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface HandleMetaAnnotation {
    public Class<?> value();

    public int priority() default 0;

    public AnnotateLoadStage stage() default AnnotateLoadStage.INIT;

    public static class HandleMetaAnnotationType implements IMetaAnnotationType {

        @Override
        public BaseAnnotation createBaseAnnotation(AnnotationMetadata annotation, ClassMetadata class_, AnnotateMod mod)
                throws AnnotateException {
            return new HandledAnnotation(annotation, class_, mod);
        }

        @Override
        public Class<? extends Annotation> getAnnotation() {
            return HandleMetaAnnotation.class;
        }
    }

    @AnnotationFactory
    public static class HandleMetaAnnotationTypeFactory implements IAnnotationFactory {
        @Override
        public void run() throws AnnotateException {
            Annotate.addMetaAnnotation(new HandleMetaAnnotationType());
        }
    }
}
