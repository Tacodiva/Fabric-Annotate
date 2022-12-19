package sh.emberj.annotate.registry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;

public class SimpleRegistry<T> extends FreezableRegistry<T> implements Iterable<T> {

    protected final Map<Identifier, T> _registry;

    public SimpleRegistry(Identifier id, Class<T> typeClass) {
        super(id, typeClass);
        _registry = new HashMap<>();
    }

    @Override
    public void register(Identifier key, T value) throws AnnotateException {
        ensureNotFrozen();
        if (_registry.putIfAbsent(key, value) != null) {
            Annotate.LOG.warn("Key '" + key + "' is already present in the registry '" + getIdentifier()
                    + "'. Replacing with new value.");
            _registry.put(key, value);
        }
    }

    public T get(Identifier key) {
        return _registry.get(key);
    }

    @Override
    public Iterator<T> iterator() {
        return _registry.values().iterator();
    }
}