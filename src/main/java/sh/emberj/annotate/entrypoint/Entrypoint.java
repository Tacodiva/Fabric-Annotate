package sh.emberj.annotate.entrypoint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sh.emberj.annotate.core.FabricLoadStage;
import sh.emberj.annotate.core.handled.HandleMetaAnnotation;

@HandleMetaAnnotation(value = EntrypointMethodHandler.class, stage = FabricLoadStage.PRELAUNCH, priority = 100000)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Entrypoint {
    public FabricLoadStage stage() default FabricLoadStage.INIT;

    public int priority() default 0;
}
