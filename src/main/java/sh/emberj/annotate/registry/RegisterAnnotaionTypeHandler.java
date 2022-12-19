package sh.emberj.annotate.registry;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateScan;
import sh.emberj.annotate.core.AnnotatedType;
import sh.emberj.annotate.core.AnnotatedTypeHandler;
import sh.emberj.annotate.core.LoadStage;
import sh.emberj.annotate.registry.RegisterTypeHandler.RegisterAnnotationInfo;

@AnnotateScan
public class RegisterAnnotaionTypeHandler extends AnnotatedTypeHandler {

    public RegisterAnnotaionTypeHandler() {
        super(LoadStage.PRELAUNCH, -200);
    }

    @Override
    public void handle(AnnotatedType type) throws AnnotateException {
        RegisterAnnotation[] annotations = tryGetAnnotations(type, RegisterAnnotation.class);
        for (RegisterAnnotation annotation : annotations) {
            RegisterTypeHandler.addInfo(new RegisterAnnotationInfo(type.getAsClass(), annotation.registry(),
                    annotation.loadStage()));
        }
    }
}
