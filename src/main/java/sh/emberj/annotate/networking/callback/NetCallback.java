package sh.emberj.annotate.networking.callback;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraft.network.NetworkSide;
import sh.emberj.annotate.core.AnnotateAnnotation;

@AnnotateAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NetCallback {
    public NetworkSide value();

    public boolean executeAsync() default false;

    public String path() default "";

    public String namespace() default "";

}
