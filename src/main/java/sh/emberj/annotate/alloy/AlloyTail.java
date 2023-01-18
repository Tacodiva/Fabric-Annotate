package sh.emberj.annotate.alloy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sh.emberj.annotate.alloy.AlloyTail.AlloyTailHandler;
import sh.emberj.annotate.alloy.types.AlloyInjectMethodType;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadataFactory;
import sh.emberj.annotate.core.handled.HandleMetaAnnotation;
import sh.emberj.annotate.core.handled.IMethodAnnotationHandler;

@HandleMetaAnnotation(value = AlloyTailHandler.class, stage = AnnotateLoadStage.PRELAUNCH, priority = 5000)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AlloyTail {
    public Class<?> value();

    public boolean cancellable() default true;

    public String name() default "";

    public static class AlloyTailHandler implements IMethodAnnotationHandler {
        public static final AlloyInjectMethodType METHOD_TYPE = new AlloyInjectMethodType("TAIL");
        @Override
        public void handleMethodAnnotation(AnnotatedMethod method, AnnotationMetadata annotation)
                throws AnnotateException {
            IAlloyMethodType.addMethod(METHOD_TYPE, annotation, method,
                    ClassMetadataFactory.create(annotation.getClassParam("value")), annotation.getStringParam("name"));
        }
    }
}