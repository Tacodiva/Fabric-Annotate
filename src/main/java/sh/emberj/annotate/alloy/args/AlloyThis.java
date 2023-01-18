package sh.emberj.annotate.alloy.args;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import sh.emberj.annotate.alloy.AlloyArgument;
import sh.emberj.annotate.alloy.AlloyArgumentType;
import sh.emberj.annotate.alloy.AlloyArgumentTypeRegistry;
import sh.emberj.annotate.alloy.IAlloyMethod;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.registry.Register;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface AlloyThis {

    @Register(path = "this", value = AlloyArgumentTypeRegistry.ID, stage = AnnotateLoadStage.PRELAUNCH, priority = 8000)
    public static class AlloyThisType extends AlloyArgumentType {
        public AlloyThisType() {
            super(AlloyThis.class);
        }

        @Override
        public void generateASM(MethodVisitor mw, AlloyArgument arg, IAlloyMethod method) throws AnnotateException {
            if (method.getTarget().hasModifier(Modifier.STATIC))
                throw new AnnotateException("Cannot use alloy argument @AlloyThis when target method is static.");
            if (!arg.type().equals(method.getTarget().getDeclaringClass().getType()))
                throw new AnnotateException(
                        "Wrong argument type for @AlloyThis. Must be the same type as the target type (got "
                                + arg.type()
                                + " expected " + method.getTarget().getDeclaringClass().getType() + ")");
            mw.visitVarInsn(Opcodes.ALOAD, 0);
        }

    }
}
