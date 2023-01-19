package sh.emberj.annotate.alloy.args;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.injection.modify.LocalVariableDiscriminator;

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
public @interface AlloyLocal {
    public boolean print() default false;

    public int ordinal() default -1;

    public int index() default -1;

    public String[] name() default {};

    @Register(path = "local", value = AlloyMethodArgTypeRegistry.ID, stage = AnnotateLoadStage.PRELAUNCH, priority = 8000)
    public static class AlloyLocalType extends AlloyMethodArgType implements Opcodes {

        public AlloyLocalType() {
            super(AlloyLocal.class);
        }

        @Override
        public void preInject(AlloyInjector injector, AlloyMethodArg arg, IAlloyInjection injection)
                throws AnnotateException {
            injector.getCurrentTargetNode().decorate(getDecorationKey(arg),
                    new LocalVariableDiscriminator.Context(injector.getInfo(), arg.type(), false,
                            injector.getCurrentTarget(), injector.getCurrentTargetNode().getCurrentTarget()));
        }

        @Override
        public void inject(InsnList asm, AlloyInjector injector, AlloyMethodArg arg, IAlloyInjection injection)
                throws AnnotateException {
            LocalVariableDiscriminator discriminator = new LocalVariableDiscriminator(false,
                    arg.annotation().getIntParam("ordinal", -1), arg.annotation().getIntParam("index", -1),
                    arg.annotation().getArrayParamOrEmpty("name").stream().map(name -> (String) name)
                            .collect(Collectors.toSet()),
                    arg.annotation().getBooleanParam("print", false));
            LocalVariableDiscriminator.Context context = injector.getCurrentTargetNode()
                    .getDecoration(getDecorationKey(arg));
            int index = discriminator.findLocal(context);
            if (index < 0)
                throw new AnnotateException("@AlloyLocal failed to find a matching local variable.");

            asm.add(new VarInsnNode(arg.type().getOpcode(ILOAD), index));
        }

        protected String getDecorationKey(AlloyMethodArg arg) {
            return "alloy:localcontext" + arg.type();
        }
    }
}
