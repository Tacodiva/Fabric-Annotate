package sh.emberj.annotate.networking.mcnative;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sh.emberj.annotate.core.AnnotateAnnotation;

@AnnotateAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NativePacket {
    public String path() default "";

    public String namespace() default "";
}
