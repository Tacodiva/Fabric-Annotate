package sh.emberj.annotate.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sh.emberj.annotate.core.AnnotateAnnotation;
import sh.emberj.annotate.core.LoadStage;

@AnnotateAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
@Repeatable(RegisterAnnotationContainer.class)
public @interface RegisterAnnotation {
    public String registry();

    public LoadStage loadStage() default LoadStage.INIT;
}
