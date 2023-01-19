package sh.emberj.annotate.alloy;

import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.asm.MethodMetadata;
import sh.emberj.annotate.mixin.IDynamicMixinMethodGenerator;

public interface IAlloyInjection extends IDynamicMixinMethodGenerator {
    public MethodMetadata getTarget();
    
    public AnnotatedMethod getAlloyMethod();
    public AlloyMethodArg[] getAlloyArgs();
}
