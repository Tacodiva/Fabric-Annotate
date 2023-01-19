package sh.emberj.annotate.alloy.args;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.VarInsnNode;

import sh.emberj.annotate.alloy.AlloyMethodArg;
import sh.emberj.annotate.alloy.AlloyMethodArgType;
import sh.emberj.annotate.alloy.AlloyMethodArgTypeRegistry;
import sh.emberj.annotate.alloy.IAlloyInjection;
import sh.emberj.annotate.alloy.mixinext.AlloyInjector;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.registry.Register;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface AlloyThis {

    @Register(path = "this", value = AlloyMethodArgTypeRegistry.ID, stage = AnnotateLoadStage.PRELAUNCH, priority = 8000)
    public static class AlloyThisType extends AlloyMethodArgType {
        public AlloyThisType() {
            super(AlloyThis.class);
        }

        @Override
        public void preInject(AlloyInjector injector, AlloyMethodArg arg, IAlloyInjection injection)
                throws AnnotateException {
            if (injection.getTarget().hasModifier(Modifier.STATIC))
                throw new AnnotateException("Cannot use alloy argument @AlloyThis when target method is static.");
            if (!arg.type().equals(injection.getTarget().getDeclaringClass().getType()))
                throw new AnnotateException(
                        "Wrong argument type for @AlloyThis. Must be the same type as the target type (got "
                                + arg.type()
                                + " expected " + injection.getTarget().getDeclaringClass().getType() + ")");
        }

        @Override
        public void inject(InsnList asm, AlloyInjector injector, AlloyMethodArg arg, IAlloyInjection injection)
                throws AnnotateException {
            asm.add(new VarInsnNode(Opcodes.ALOAD, 0));
        }

    }
}
