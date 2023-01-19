package sh.emberj.annotate.alloy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sh.emberj.annotate.alloy.AlloyHead.AlloyHeadHandler;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadataFactory;
import sh.emberj.annotate.core.handled.HandleMetaAnnotation;
import sh.emberj.annotate.core.handled.IMethodAnnotationHandler;

@HandleMetaAnnotation(value = AlloyHeadHandler.class, stage = AnnotateLoadStage.PRELAUNCH, priority = 5000)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AlloyHead {
    public Class<?> value();

    public boolean cancellable() default true;

    public String name() default "";

    public static class AlloyHeadHandler implements IMethodAnnotationHandler {
        public static final AlloyInjectionType METHOD_TYPE = new AlloyInjectionType("HEAD");
        @Override
        public void handleMethodAnnotation(AnnotatedMethod method, AnnotationMetadata annotation)
                throws AnnotateException {
            IAlloyInjectionType.createFromMethod(METHOD_TYPE, annotation, method,
                    ClassMetadataFactory.create(annotation.getClassParam("value")), annotation.getStringParam("name"));
        }
    }
}
