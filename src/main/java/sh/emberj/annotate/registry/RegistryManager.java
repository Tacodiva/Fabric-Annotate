package sh.emberj.annotate.registry;

import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;

public class RegistryManager {
    private RegistryManager() {}

    public static final String ID = Annotate.ID + ":registries";
    
    private static SimpleRegistry<IRegistry> _registries;

    public static SimpleRegistry<IRegistry> getMetaRegistry() {
        if (_registries == null) {
            _registries = new SimpleRegistry<>(new Identifier(ID));
            // Does the registry of all registries contain itself?
            _registries.registerObject(_registries);
        }
        return _registries;
    }

    public static IRegistry getRegistry(Identifier id) {
        return getMetaRegistry().get(id);
    }

    public static void register(Identifier registry, Identifier id, Object value) throws AnnotateException {
        IRegistry registryInstance = getRegistry(registry);
        if (registryInstance == null) throw new AnnotateException("No registry found with id '"+registry+"'.");
        registryInstance.registerObject(id, value);
    }
}
