package sh.emberj.annotate.registry;

import net.minecraft.util.Identifier;

public abstract class GenericRegistry<T> implements IRegistry {

    private final Identifier _identifier;
    protected final Class<T> _typeClass;

    /**
     * @param id    The identifier of the registry
     * @param dummy This is a trick to get the type of the generic. Do not specify
     *              anything.
     * @throws IllegalArgumentException If arguments are specified after the
     *                                  identifier.
     */
    @SuppressWarnings("unchecked")
    public GenericRegistry(Identifier id, T... dummy) {
        _identifier = id;
        if (dummy == null || dummy.length > 0)
            throw new IllegalArgumentException("Do not specify the 'dummy' argument");
        _typeClass = (Class<T>) dummy.getClass().componentType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerObject(Identifier key, Object value) {
        if (!_typeClass.isAssignableFrom(value.getClass()))
            throw new IllegalArgumentException("Object of type " + value.getClass()
                    + " cannot be added to the registry " + getIdentifier() + ". Expected " + _typeClass + ".");
        register(key, (T) value);
    }

    public abstract void register(Identifier key, T value);

    @Override
    public Identifier getIdentifier() {
        return _identifier;
    }
}
