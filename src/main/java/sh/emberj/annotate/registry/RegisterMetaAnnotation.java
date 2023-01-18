package sh.emberj.annotate.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.core.MetaMetaAnnotation;

@MetaMetaAnnotation(RegisterMetaAnnotationType.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
@Repeatable(RegisterMetaAnnotationContainer.class)
public @interface RegisterMetaAnnotation {
    public String value();

    public AnnotateLoadStage stage() default AnnotateLoadStage.INIT;

    public int priority() default 0;
}
