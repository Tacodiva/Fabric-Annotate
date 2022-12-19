package sh.emberj.annotate.registry;

import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.AnnotateException;

public abstract class GenericRegistry<T> implements IRegistry {

    private final Identifier _identifier;
    protected final Class<T> _typeClass;

    public GenericRegistry(Identifier id, Class<T> typeClass) {
        _identifier = id;
        _typeClass = typeClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerObject(Identifier key, Object value) throws AnnotateException {
        if (!_typeClass.isAssignableFrom(value.getClass()))
            throw new AnnotateException("Object of type " + value.getClass()
                    + " cannot be added to the registry " + getIdentifier() + ". Expected " + _typeClass + ".");
        register(key, (T) value);
    }

    public abstract void register(Identifier key, T value) throws AnnotateException;

    @Override
    public Identifier getIdentifier() {
        return _identifier;
    }
}
