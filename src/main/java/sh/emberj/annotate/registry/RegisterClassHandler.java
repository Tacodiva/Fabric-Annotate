package sh.emberj.annotate.registry;

import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedClass;
import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.handled.IClassAnnotationHandler;

public class RegisterClassHandler implements IClassAnnotationHandler {
    @Override
    public void handleClassAnnotation(AnnotatedClass class_, AnnotationMetadata annotation) throws AnnotateException {
        Annotate.addLoadListener(
                new FutureRegistration(class_,
                        annotation.getStringParam("value"),
                        annotation.getStringParam("path"),
                        annotation.getStringParam("namespace"),
                        annotation.getEnumParam("stage", AnnotateLoadStage.class, AnnotateLoadStage.PREINIT),
                        annotation.getIntParam("priority", 0)));
    }
}
