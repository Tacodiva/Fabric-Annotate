package sh.emberj.annotate.alloy.args;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import sh.emberj.annotate.alloy.AlloyArgument;
import sh.emberj.annotate.alloy.AlloyArgumentType;
import sh.emberj.annotate.alloy.AlloyArgumentTypeRegistry;
import sh.emberj.annotate.alloy.IAlloyMethod;
import sh.emberj.annotate.alloy.types.AlloyInjectMethodType.AlloyInjectMethod;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.registry.Register;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface AlloyInfo {

    @Register(path = "info", value = AlloyArgumentTypeRegistry.ID, stage = AnnotateLoadStage.PRELAUNCH, priority = 8000)
    public static class AlloyInfoType extends AlloyArgumentType {

        public static final Type TYPE_CALLBACK_INFO = Type.getType(CallbackInfo.class);
        public static final Type TYPE_CALLBACK_RETURNABLE = Type.getType(CallbackInfoReturnable.class);

        public AlloyInfoType() {
            super(AlloyInfo.class);
        }

        @Override
        public void generateASM(MethodVisitor mw, AlloyArgument arg, IAlloyMethod method) throws AnnotateException {
            if (!(method instanceof AlloyInjectMethod injectMethod))
                throw new AnnotateException(
                        "The @AlloyInfo annotation can only be used on inject methods like @AlloyHead or @AlloyTail.");

            boolean isReturnable = arg.type().equals(TYPE_CALLBACK_RETURNABLE);
            boolean isInfo = arg.type().equals(TYPE_CALLBACK_INFO);

            if (!(isReturnable || isInfo))
                throw new AnnotateException(
                        "Wrong argument type for @AlloyInfo. Must be the either CallbackInfo or CallbackInfoReturnable.");

            if (injectMethod.TARGET.getReturnType().equals(Type.VOID_TYPE) && isReturnable)
                throw new AnnotateException(
                        "Cannot use CallbackInfoReturnable when targeting a method that doesn't return anything. Use CallbackInfo instead.");

            mw.visitVarInsn(Opcodes.ALOAD, injectMethod.getCallbackParamIndex());
        }
    }
}
