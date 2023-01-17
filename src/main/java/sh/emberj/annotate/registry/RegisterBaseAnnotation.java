package sh.emberj.annotate.registry;

import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateMod;
import sh.emberj.annotate.core.AnnotatedClass;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.BaseAnnotation;
import sh.emberj.annotate.core.FabricLoadStage;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadata;

public class RegisterBaseAnnotation extends BaseAnnotation {
    private final FabricLoadStage _STAGE;
    private final String _REGISTRY;
    private final int _PRIORITY;

    public RegisterBaseAnnotation(AnnotationMetadata metadata, ClassMetadata class_, AnnotateMod mod)
            throws AnnotateException {
        super(metadata, class_, mod);
        _REGISTRY = metadata.getStringParam("value");
        _STAGE = metadata.getEnumParam("stage", FabricLoadStage.class, FabricLoadStage.PREINIT);
        _PRIORITY = metadata.getIntParam("priority", 0);
    }

    @Override
    public void handleInstance(AnnotationMetadata instance, AnnotatedClass annotatedClass) throws AnnotateException {
        Annotate.addLoadListener(new FutureRegistration(annotatedClass, _REGISTRY, instance.getStringParam("path"),
                instance.getStringParam("namespace"), _STAGE, _PRIORITY));
    }

    @Override
    public void handleInstance(AnnotationMetadata instance, AnnotatedMethod annotatedMethod) throws AnnotateException {
        throw new AnnotateException(
                "Cannot annotation methods with annotations annotated with with @RegisterMetaAnnotation.",
                annotatedMethod);
    }

}
