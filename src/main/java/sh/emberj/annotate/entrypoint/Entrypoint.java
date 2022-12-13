package sh.emberj.annotate.entrypoint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sh.emberj.annotate.core.AnnotateAnnotation;
import sh.emberj.annotate.core.LoadStage;

@AnnotateAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Entrypoint {
    public LoadStage stage() default LoadStage.INIT;
    public int priority() default 0;
}
