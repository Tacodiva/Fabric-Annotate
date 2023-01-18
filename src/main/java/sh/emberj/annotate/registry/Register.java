package sh.emberj.annotate.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.core.handled.HandleMetaAnnotation;

@HandleMetaAnnotation(value = RegisterClassHandler.class, stage = AnnotateLoadStage.PRELAUNCH, priority = 50000)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Repeatable(RegisterContainer.class)
public @interface Register {
    public String value();

    public String path() default "";

    public String namespace() default "";

    public AnnotateLoadStage stage() default AnnotateLoadStage.PREINIT;

    public int priority() default 0;
}
