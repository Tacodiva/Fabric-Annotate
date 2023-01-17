package sh.emberj.annotate.entrypoint;

import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.FabricLoadStage;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.handled.IMethodAnnotationHandler;

public class EntrypointMethodHandler implements IMethodAnnotationHandler {

    @Override
    public void handleMethodAnnotation(AnnotatedMethod method, AnnotationMetadata annotation)
            throws AnnotateException {
        Annotate.addLoadListener(new EntrypointInstance(
                annotation.getEnumParam("stage", FabricLoadStage.class, FabricLoadStage.INIT),
                annotation.getIntParam("priority", 0),
                method));
    }
}
