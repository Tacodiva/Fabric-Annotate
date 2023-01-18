package sh.emberj.annotate.registry;

import net.minecraft.util.Identifier;

public abstract class FreezableRegistry<T> extends GenericRegistry<T> {

    private boolean _frozen;

    public FreezableRegistry(Identifier id, Class<T> typeClass) {
        super(id, typeClass);
    }

    public boolean isFrozen() {
        return _frozen;
    }

    protected void ensureNotFrozen() {
        if (_frozen)
            throw new IllegalStateException("Registry is frozen.");
    }

    protected void ensureFrozen() {
        if (!_frozen)
            throw new IllegalStateException("Registry is not frozen.");
    }

    protected void freeze() {
        ensureNotFrozen();
        _frozen = true;
    }

    protected void tryFreeze() {
        _frozen = true;
    }
}
