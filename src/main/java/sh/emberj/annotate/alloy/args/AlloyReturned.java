package sh.emberj.annotate.alloy.args;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.include.com.google.common.collect.ImmutableSet;

import sh.emberj.annotate.alloy.AlloyArgument;
import sh.emberj.annotate.alloy.AlloyArgumentType;
import sh.emberj.annotate.alloy.AlloyArgumentTypeRegistry;
import sh.emberj.annotate.alloy.IAlloyMethod;
import sh.emberj.annotate.alloy.types.AlloyInjectMethodType.AlloyInjectMethod;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.core.Utils;
import sh.emberj.annotate.registry.Register;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface AlloyReturned {

    @Register(path = "returned", value = AlloyArgumentTypeRegistry.ID, stage = AnnotateLoadStage.PRELAUNCH, priority = 8000)
    public static class AlloyReturnedType extends AlloyArgumentType implements Opcodes {
        private static final ImmutableSet<String> _VALID_POSITIONS = ImmutableSet.of("TAIL");

        public AlloyReturnedType() {
            super(AlloyReturned.class);
        }

        @Override
        public void generateASM(MethodVisitor mw, AlloyArgument arg, IAlloyMethod method) throws AnnotateException {
            Type targetReturn = method.getTarget().getReturnType();

            if (!(method instanceof AlloyInjectMethod injectMethod))
                throw new AnnotateException(
                        "The @AlloyReturned annotation can only be used on return inject methods like @AlloyTail.");
            if (targetReturn.equals(Type.VOID_TYPE))
                throw new AnnotateException(
                        "The @AlloyReturned annotation can only be used on methods who's targets have a return value.");
            if (!_VALID_POSITIONS.contains(injectMethod.getPosition()))
                throw new AnnotateException(
                        " @AlloyReturned annotation can only be used on return inject methods like @AlloyTail.");
            if (!targetReturn.equals(arg.type()))
                throw new AnnotateException(
                        "Wrong argument type for @AlloyReturned. Must be the same type as the return type of the target method. (got "
                                + arg.type() + " expected " + targetReturn + " )");

            mw.visitVarInsn(ALOAD, injectMethod.getCallbackParamIndex());
            mw.visitMethodInsn(INVOKEVIRTUAL, Type.getType(CallbackInfoReturnable.class).getInternalName(),
                    "getReturnValue",
                    "()Ljava/lang/Object;", false);
            if (Utils.isPrimitive(targetReturn))
                Utils.convertFromObject(mw, targetReturn);
        }
    }
}
