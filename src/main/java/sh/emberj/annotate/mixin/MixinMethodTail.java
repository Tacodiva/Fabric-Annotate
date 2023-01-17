package sh.emberj.annotate.mixin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sh.emberj.annotate.core.FabricLoadStage;
import sh.emberj.annotate.core.handled.HandleMetaAnnotation;

@HandleMetaAnnotation(value = MixinMethodTailHandler.class, stage = FabricLoadStage.PRELAUNCH, priority = -5000)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MixinMethodTail {
    public Class<?> type();

    public String method() default "";
}
