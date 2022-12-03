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
@Target({ ElementType.TYPE })
@Repeatable(RegisterContainer.class)
public @interface Register {
    public String registry();

    public String path() default "";

    public String namespace() default "";

    public LoadStage loadStage() default LoadStage.INIT;
}
