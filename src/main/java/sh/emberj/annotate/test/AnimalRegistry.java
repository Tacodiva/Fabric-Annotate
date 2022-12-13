package sh.emberj.annotate.test;

import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.Instance;
import sh.emberj.annotate.registry.Register;
import sh.emberj.annotate.registry.RegistryManager;
import sh.emberj.annotate.registry.SimpleRegistry;

@Register(registry = RegistryManager.ID)
public class AnimalRegistry extends SimpleRegistry<Animal> {

    @Instance
    public static AnimalRegistry INSTANCE;
    public static final String ID = "annotate:animals";

    public AnimalRegistry() {
        super(new Identifier(ID));
    }
}
