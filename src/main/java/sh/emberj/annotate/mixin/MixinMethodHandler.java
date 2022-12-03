package sh.emberj.annotate.mixin;

import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateScan;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.AnnotatedMethodHandler;
import sh.emberj.annotate.core.LoadStage;

@AnnotateScan
public class MixinMethodHandler extends AnnotatedMethodHandler {

    public MixinMethodHandler() {
        super(LoadStage.PRELAUNCH);
    }

    @Override
    public void handle(AnnotatedMethod method) throws AnnotateException {
        MixinMethodHead mixinHead = tryGetAnnotation(method, MixinMethodHead.class);

        if (mixinHead != null) {
            Annotate.LOG.info("Found mixin method " + method);
            Annotate.LOG.info("Targeting " + mixinHead.type());
        }
        
    }
    
}
