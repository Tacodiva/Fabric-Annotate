package sh.emberj.annotate.mixin;

import org.objectweb.asm.Type;
import org.spongepowered.asm.service.IClassTracker;
import org.spongepowered.asm.service.MixinService;

import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateScan;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.AnnotatedMethodHandler;
import sh.emberj.annotate.core.LoadStage;
import sh.emberj.annotate.core.asm.AnnotatedMethodMeta;
import sh.emberj.annotate.core.asm.AnnotationMeta;
import sh.emberj.annotate.mixin.asm.DynamicMixinClass;
import sh.emberj.annotate.mixin.asm.InjectMethodGenerator;
import sh.emberj.annotate.mixin.asm.InjectMethodGenerator.InjectPosition;

@AnnotateScan
public class MixinMethodHandler extends AnnotatedMethodHandler {

    public MixinMethodHandler() {
        super(LoadStage.PRELAUNCH);
    }

    @Override
    public void handle(AnnotatedMethod mixinMethod) throws AnnotateException {
        AnnotatedMethodMeta mixinMeta = mixinMethod.getMeta();
        AnnotationMeta mixinAnnotation = mixinMeta.getAnnotationByType(MixinMethodHead.class);
        if (mixinAnnotation != null) {
            Type targetType = mixinAnnotation.getTypeParam("type");
            if (MixinService.getService().getClassTracker().isClassLoaded(targetType.getClassName()))
                throw new AnnotateException("Mixin targeting a class that has already been loaded!");
            DynamicMixinClass mixinClass = AnnotateMixins.getMixinClass(targetType);
            InjectMethodGenerator mixin = new InjectMethodGenerator(mixinMethod, InjectPosition.HEAD, targetType,
                    mixinAnnotation);
            mixinClass.addMethod(mixin);
        }
    }

}
