package sh.emberj.annotate.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sh.emberj.annotate.core.AnnotateAnnotation;
import sh.emberj.annotate.registry.RegisterAnnotation;

@AnnotateAnnotation
@RegisterAnnotation(registry = AnimalRegistry.ID)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface RegisterAnimal {
    
}
