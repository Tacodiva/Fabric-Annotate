package sh.emberj.annotate.alloy;

import sh.emberj.annotate.core.asm.MethodMetadata;
import sh.emberj.annotate.mixin.IDynamicMixinMethodGenerator;

public interface IAlloyMethod extends IDynamicMixinMethodGenerator {
    public MethodMetadata getTarget();
    
    public MethodMetadata getMethod();
    public AlloyArgument[] getMethodArgs();

}
