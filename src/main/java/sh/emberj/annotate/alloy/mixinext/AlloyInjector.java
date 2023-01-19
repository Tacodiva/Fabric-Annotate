package sh.emberj.annotate.alloy.mixinext;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes.InjectionNode;
import org.spongepowered.asm.mixin.injection.struct.Target;

public class AlloyInjector extends Injector {

    public AlloyInjector(InjectionInfo info) {
        super(info, "@AlloyInject");
    }

    @Override
    protected void inject(Target target, InjectionNode node) {
        target.insertBefore(node, methodNode.instructions);
        
        methodNode.instructions.add(new InsnNode(Opcodes.RETURN));

        info.addCallbackInvocation(null);
    }
}