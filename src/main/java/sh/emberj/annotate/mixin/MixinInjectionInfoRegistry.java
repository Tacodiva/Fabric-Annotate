package sh.emberj.annotate.mixin;

import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;

import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.registry.GenericRegistry;
import sh.emberj.annotate.registry.Register;
import sh.emberj.annotate.registry.RegistryManager;

@Register(value = RegistryManager.ID, stage = AnnotateLoadStage.PRELAUNCH, priority = 9000)
public class MixinInjectionInfoRegistry extends GenericRegistry<InjectionInfo> {

    public static final String ID = "annotate:mixin_injectors";

    public MixinInjectionInfoRegistry() {
        super(new Identifier(ID), InjectionInfo.class);
    }

    @Override
    public void register(Identifier key, InjectionInfo value) throws AnnotateException {
        InjectionInfo.register(_typeClass);
        
    }

}
