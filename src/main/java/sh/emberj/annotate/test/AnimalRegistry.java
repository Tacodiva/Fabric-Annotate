package sh.emberj.annotate.test;

import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.Instance;
import sh.emberj.annotate.registry.Registry;
import sh.emberj.annotate.registry.SimpleRegistry;

@Registry
public class AnimalRegistry extends SimpleRegistry<Animal> {

    @Instance
    public static AnimalRegistry INSTANCE;
    
    public static final String ID = "annotate:animals";

    public AnimalRegistry() {
        super(new Identifier(ID), Animal.class);
    }
}
