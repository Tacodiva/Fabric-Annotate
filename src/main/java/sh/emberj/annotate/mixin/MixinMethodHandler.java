package sh.emberj.annotate.mixin;

import org.objectweb.asm.Type;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateScan;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.AnnotatedMethodHandler;
import sh.emberj.annotate.core.LoadStage;
import sh.emberj.annotate.core.asm.AnnotationMeta;
import sh.emberj.annotate.mixin.asm.InjectMethodGenerator;
import sh.emberj.annotate.mixin.asm.InjectMethodGenerator.InjectPosition;

@AnnotateScan
public class MixinMethodHandler extends AnnotatedMethodHandler {

    public MixinMethodHandler() {
        super(LoadStage.PRELAUNCH, -1000);
    }

    @Override
    public void handle(AnnotatedMethod mixinMethod) throws AnnotateException {
        tryHandle(mixinMethod, MixinMethodHead.class, InjectPosition.HEAD);
        tryHandle(mixinMethod, MixinMethodTail.class, InjectPosition.TAIL);
    }

    private void tryHandle(AnnotatedMethod mixinMethod, Class<?> annotation, InjectPosition position)
            throws AnnotateException {
        AnnotationMeta mixinAnnotation = mixinMethod.getMeta().getAnnotationByType(annotation);
        if (mixinAnnotation != null) {
            Type targetType = mixinAnnotation.getTypeParam("type");
            AnnotateMixins.addMixin(new InjectMethodGenerator(mixinMethod, position, targetType,
                    mixinAnnotation));
        }
    }

    @Override
    public void postHandle() {
        try {
            AnnotateMixins.runMixins();
        } catch (AnnotateException e) {
            throw new RuntimeException(e);
        }
    }
}
