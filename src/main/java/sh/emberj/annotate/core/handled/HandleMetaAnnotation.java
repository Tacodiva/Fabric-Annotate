package sh.emberj.annotate.core.handled;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sh.emberj.annotate.core.FabricLoadStage;
import sh.emberj.annotate.core.MetaMetaAnnotation;

@MetaMetaAnnotation(HandleMetaAnnotationType.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface HandleMetaAnnotation {
    public Class<?> value();
    public int priority() default 0;
    public FabricLoadStage stage() default FabricLoadStage.INIT;
}
