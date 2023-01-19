package sh.emberj.annotate.alloy.mixinext;

import java.util.Collection;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.InjectionPoint.AtCode;
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;

import sh.emberj.annotate.mixin.MixinExtInjectionPoint;

@MixinExtInjectionPoint
@AtCode("ALLOY")
public class AlloyInjectionPoint extends InjectionPoint {

    public AlloyInjectionPoint(InjectionPointData data) {

    }

    @Override
    public boolean find(String desc, InsnList insns, Collection<AbstractInsnNode> nodes) {
        nodes.add(insns.getFirst());
        return true;
    }

}
