package sh.emberj.annotate.registry;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
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
@Target({ ElementType.ANNOTATION_TYPE })
@Repeatable(RegisterMetaAnnotationContainer.class)
public @interface RegisterMetaAnnotation {
    public String value();

    public AnnotateLoadStage stage() default AnnotateLoadStage.INIT;

    public int priority() default 0;

    public static class RegisterMetaAnnotationType implements IMetaAnnotationType {
    
        @Override
        public BaseAnnotation createBaseAnnotation(AnnotationMetadata instance, ClassMetadata class_, AnnotateMod mod)
                throws AnnotateException {
            return new RegisterBaseAnnotation(instance, class_, mod);
        }

        @Override
        public Class<? extends Annotation> getAnnotation() {
            return RegisterMetaAnnotation.class;
        }
    }

    @AnnotationFactory
    public static class RegisterMetaAnnotationTypeFactory implements IAnnotationFactory {
        @Override
        public void run() throws AnnotateException {
            Annotate.addMetaAnnotation(new RegisterMetaAnnotationType());
        }
    }
    
}
