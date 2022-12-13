package sh.emberj.annotate.mixin.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public interface IDynamicMixinMethodGenerator extends Opcodes {

    public DynamicMixinAnnotation generateAnnotation();

    public String generateDescriptor();

    public int generateMethodFlags();

    public void generateMethod(MethodVisitor mw);

    public String getNamePrefix();

}
