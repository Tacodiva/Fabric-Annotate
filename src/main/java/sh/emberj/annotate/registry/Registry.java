package sh.emberj.annotate.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sh.emberj.annotate.core.AnnotateLoadStage;

@RegisterMetaAnnotation(value = RegistryManager.ID, stage = AnnotateLoadStage.PREINIT, priority = 500)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Registry {
    public String path() default "";

    public String namespace() default "";
}
