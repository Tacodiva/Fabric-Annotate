package sh.emberj.annotate.mixin.asm;

import org.objectweb.asm.MethodVisitor;

public interface IDynamicMixinMethodGenerator {

    public DynamicMixinAnnotation generateAnnotation();
    public String generateDescriptor();
    public void generateMethod(MethodVisitor mw);
    public String getNamePrefix();
    
}
