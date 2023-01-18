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
public class AlloyArgumentTypeRegistry extends GenericRegistry<AlloyArgumentType> {
    @Instance
    public static final AlloyArgumentTypeRegistry INSTANCE = new AlloyArgumentTypeRegistry();
    public static final String ID = "annotate:alloy_argument_type";

    private final Map<String, AlloyArgumentType> _REGISTRY;

    public AlloyArgumentTypeRegistry() {
        super(new Identifier(ID), AlloyArgumentType.class);
        _REGISTRY = new HashMap<>();
    }

    @Override
    public void register(Identifier key, AlloyArgumentType value) throws AnnotateException {
        _REGISTRY.put(Type.getType(value.getAnnotation()).toString(), value);
    }

    public AlloyArgumentType getArgument(Type annotation) {
        return _REGISTRY.get(annotation.toString());
    }

}
