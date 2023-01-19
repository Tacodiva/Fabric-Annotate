package sh.emberj.annotate.alloy;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;

import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.core.Instance;
import sh.emberj.annotate.registry.GenericRegistry;
import sh.emberj.annotate.registry.Register;
import sh.emberj.annotate.registry.RegistryManager;

@Register(value = RegistryManager.ID, stage = AnnotateLoadStage.PRELAUNCH, priority = 9000)
public class AlloyMethodArgTypeRegistry extends GenericRegistry<AlloyMethodArgType> {
    @Instance
    public static final AlloyMethodArgTypeRegistry INSTANCE = new AlloyMethodArgTypeRegistry();
    public static final String ID = "annotate:alloy_argument_type";

    private final Map<String, AlloyMethodArgType> _REGISTRY;

    public AlloyMethodArgTypeRegistry() {
        super(new Identifier(ID), AlloyMethodArgType.class);
        _REGISTRY = new HashMap<>();
    }

    @Override
    public void register(Identifier key, AlloyMethodArgType value) throws AnnotateException {
        _REGISTRY.put(Type.getType(value.getAnnotation()).toString(), value);
    }

    public AlloyMethodArgType getArgument(Type annotation) {
        return _REGISTRY.get(annotation.toString());
    }

}
