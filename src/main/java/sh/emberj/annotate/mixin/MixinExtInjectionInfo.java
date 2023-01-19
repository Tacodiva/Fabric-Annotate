package sh.emberj.annotate.mixin;

import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;

import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedClass;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.AnnotationFactory;
import sh.emberj.annotate.core.BaseAnnotation;
import sh.emberj.annotate.core.IAnnotationFactory;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadataFactory;

public @interface MixinExtInjectionInfo {

    @AnnotationFactory
    public class InjectionInfoHandlerFactory implements IAnnotationFactory {
        @Override
        public void run() throws AnnotateException {
            Annotate.addAnnotation(new InjectionInfoHandler());
        }
    }

    public static class InjectionInfoHandler extends BaseAnnotation {
        public InjectionInfoHandler() throws AnnotateException {
            super(ClassMetadataFactory.create(Type.getType(MixinExtInjectionInfo.class)),
                    Annotate.getMod(Annotate.ID));
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleInstance(AnnotationMetadata instance, AnnotatedClass annotatedClass)
                throws AnnotateException {
            Class<?> infoClass = annotatedClass.getMetadata().getAsClass();
            if (!InjectionInfo.class.isAssignableFrom(infoClass))
                throw new AnnotateException(
                        "Types annotated with @MixinExtInjectionInfo must extend InjectionInfo.",
                        annotatedClass);
            InjectionInfo.register((Class<? extends InjectionInfo>) infoClass);
        }

        @Override
        public void handleInstance(AnnotationMetadata instance, AnnotatedMethod annotatedMethod)
                throws AnnotateException {
            throw new RuntimeException();
        }
    }
}
