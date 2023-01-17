package sh.emberj.annotate.mixin;

import org.objectweb.asm.Type;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.handled.IMethodAnnotationHandler;
import sh.emberj.annotate.mixin.asm.InjectMethodGenerator;
import sh.emberj.annotate.mixin.asm.InjectMethodGenerator.InjectPosition;

public class MixinMethodTailHandler implements IMethodAnnotationHandler {

    @Override
    public void handleMethodAnnotation(AnnotatedMethod method, AnnotationMetadata annotation)
            throws AnnotateException {
        Type targetType = annotation.getClassParam("type");
        AnnotateMixins.addMixin(new InjectMethodGenerator(method, InjectPosition.TAIL, targetType, annotation));
    }

}
