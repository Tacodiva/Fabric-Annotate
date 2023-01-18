package sh.emberj.annotate.entrypoint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.core.handled.HandleMetaAnnotation;

@HandleMetaAnnotation(value = EntrypointMethodHandler.class, stage = AnnotateLoadStage.PRELAUNCH, priority = 100000)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Entrypoint {
    public AnnotateLoadStage stage() default AnnotateLoadStage.INIT;

    public int priority() default 0;
}
