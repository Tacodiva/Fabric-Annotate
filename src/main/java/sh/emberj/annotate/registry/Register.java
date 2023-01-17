package sh.emberj.annotate.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sh.emberj.annotate.core.FabricLoadStage;
import sh.emberj.annotate.core.handled.HandleMetaAnnotation;

@HandleMetaAnnotation(value = RegisterClassHandler.class, stage = FabricLoadStage.PRELAUNCH, priority = -90000)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Repeatable(RegisterContainer.class)
public @interface Register {
    public String registry();

    public String path() default "";

    public String namespace() default "";

    public FabricLoadStage stage() default FabricLoadStage.PREINIT;

    public int priority() default 0;
}
