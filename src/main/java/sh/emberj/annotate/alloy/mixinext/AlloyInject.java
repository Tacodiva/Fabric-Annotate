package sh.emberj.annotate.alloy.mixinext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.spongepowered.asm.mixin.injection.At;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AlloyInject {
    public String[] method();

    public At[] at();

    public int require() default -1;
}
