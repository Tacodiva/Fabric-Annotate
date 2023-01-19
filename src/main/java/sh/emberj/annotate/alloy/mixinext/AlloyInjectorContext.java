package sh.emberj.annotate.alloy.mixinext;

import org.spongepowered.asm.mixin.injection.struct.Target;

public record AlloyInjectorContext(AlloyInjector inject, Target target) {

    public int allocateLocals(int size) {
        return target.allocateLocals(size);
    }

}
