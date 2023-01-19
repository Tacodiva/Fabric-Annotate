package sh.emberj.annotate.alloy.args;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
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
public @interface AlloyReturned {

    @Register(path = "returned", value = AlloyMethodArgTypeRegistry.ID, stage = AnnotateLoadStage.PRELAUNCH, priority = 8000)
    public static class AlloyReturnedType extends AlloyMethodArgType implements Opcodes {
        public AlloyReturnedType() {
            super(AlloyReturned.class);
        }

        @Override
        public void preInject(AlloyInjector injector, AlloyMethodArg arg, IAlloyInjection injection)
                throws AnnotateException {
            Type targetReturn = injection.getTarget().getReturnType();

            if (targetReturn.equals(Type.VOID_TYPE))
                throw new AnnotateException(
                        "The @AlloyReturned annotation can only be used on methods who's targets have a return value.");
            if (!targetReturn.equals(arg.type()))
                throw new AnnotateException(
                        "Wrong argument type for @AlloyReturned. Must be the same type as the return type of the target method. (got "
                                + arg.type() + " expected " + targetReturn + " )");

            int targetOpcode = injector.getCurrentTargetNode().getCurrentTarget().getOpcode();
            if (targetOpcode > ARETURN || targetOpcode < IRETURN)
                throw new AnnotateException(
                        "The @AlloyReturned annotation can only be used on methods which target a return statement, like @AlloyTail.");

            injector.allocateReturnLVI();
        }

        @Override
        public void inject(InsnList asm, AlloyInjector injector, AlloyMethodArg arg, IAlloyInjection injection)
                throws AnnotateException {
            asm.add(new VarInsnNode(injection.getTarget().getReturnType().getOpcode(ILOAD), injector.getReturnLVI()));
        }

    }
}
