package sh.emberj.annotate.mixin;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import sh.emberj.annotate.core.AnnotateException;

public interface IDynamicMixinMethodGenerator extends Opcodes {

    public static final Type TYPE_CALLBACK_INFO = Type.getType(CallbackInfo.class);
    public static final Type TYPE_CALLBACK_RETURNABLE = Type.getType(CallbackInfoReturnable.class);

    public DynamicMixinAnnotation generateAnnotation() throws AnnotateException;

    public String generateDescriptor() throws AnnotateException;

    public int generateMethodFlags() throws AnnotateException;

    public void generateMethod(MethodVisitor mw) throws AnnotateException;

    public String getNamePrefix() throws AnnotateException;

    public Type getTargetType() throws AnnotateException;

}
