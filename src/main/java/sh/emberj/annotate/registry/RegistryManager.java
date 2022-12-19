package sh.emberj.annotate.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;

public class RegistryManager {
    private RegistryManager() {}

    public static final String ID = Annotate.ID + ":registries";

    public static boolean logRegistrations = true;
    public static final Logger LOG = LoggerFactory.getLogger("Annotate/Registry");
    
    private static SimpleRegistry<IRegistry> _registries;

    public static SimpleRegistry<IRegistry> getMetaRegistry() {
        if (_registries == null) {
            _registries = new SimpleRegistry<>(new Identifier(ID), IRegistry.class);
            // Does the registry of all registries contain itself?
            try {
                _registries.registerObject(_registries);
            } catch (AnnotateException e) {
                throw new RuntimeException(e);
            }
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
        if (logRegistrations)
            LOG.info("Registered '" + id + "' into '" + registry + "'");
    }
}
