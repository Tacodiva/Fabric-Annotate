package sh.emberj.annotate.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sh.emberj.annotate.core.AnnotateAnnotation;

@AnnotateAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface RegisterAnnotationContainer {
    RegisterAnnotation[] value();
}
