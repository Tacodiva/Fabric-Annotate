package sh.emberj.annotate.alloy.mixinext;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.HandlerPrefix;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;
import org.spongepowered.asm.util.Annotations;

import sh.emberj.annotate.mixin.MixinExtInjectionInfo;

@MixinExtInjectionInfo
@InjectionInfo.AnnotationType(AlloyInject.class)
@HandlerPrefix("modifyReceiver")
public class AlloyInjectInfo extends InjectionInfo {

    public AlloyInjectInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
        super(mixin, method, annotation);
    }

    @Override
    public Injector parseInjector(AnnotationNode arg0) {
        return new AlloyInjector(this, Annotations.<Integer>getValue(annotation, "annotateID"));
    }

}
