package sh.emberj.annotate.mixin;

import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.injection.InjectionPoint;

import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedClass;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.AnnotationFactory;
import sh.emberj.annotate.core.BaseAnnotation;
import sh.emberj.annotate.core.IAnnotationFactory;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadataFactory;

public @interface MixinExtInjectionPoint {

    public String namespace() default "";

    @AnnotationFactory
    public class InjectionPointHandlerFactory implements IAnnotationFactory {
        @Override
        public void run() throws AnnotateException {
            Annotate.addAnnotation(new InjectionPointHandler());
        }
    }

    public static class InjectionPointHandler extends BaseAnnotation {
        public InjectionPointHandler() throws AnnotateException {
            super(ClassMetadataFactory.create(Type.getType(MixinExtInjectionPoint.class)),
                    Annotate.getMod(Annotate.ID));
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleInstance(AnnotationMetadata instance, AnnotatedClass annotatedClass)
                throws AnnotateException {
            Class<?> infoClass = annotatedClass.getMetadata().getAsClass();
            if (!InjectionPoint.class.isAssignableFrom(infoClass))
                throw new AnnotateException(
                        "Types annotated with @MixinExtInjectionPoint must extend InjectionInfo.",
                        annotatedClass);
            String namespace = instance.getStringParam("namespace");
            if (namespace == null) namespace = annotatedClass.getMod().getID();
            InjectionPoint.register((Class<? extends InjectionPoint>) infoClass, namespace);
        }

        @Override
        public void handleInstance(AnnotationMetadata instance, AnnotatedMethod annotatedMethod)
                throws AnnotateException {
            throw new RuntimeException();
        }
    }
}
