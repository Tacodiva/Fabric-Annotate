package sh.emberj.annotate.mixin.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public interface IDynamicMixinMethodGenerator extends Opcodes {

    public static final Type TYPE_CALLBACK_INFO = Type.getType(CallbackInfo.class);
    public static final Type TYPE_CALLBACK_RETURNABLE = Type.getType(CallbackInfoReturnable.class);

    public DynamicMixinAnnotation generateAnnotation();

    public String generateDescriptor();

    public int generateMethodFlags();

    public void generateMethod(MethodVisitor mw);

    public String getNamePrefix();

    public Type getTargetType();

}
