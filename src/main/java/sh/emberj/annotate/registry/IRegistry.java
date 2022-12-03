package sh.emberj.annotate.registry;

import net.minecraft.util.Identifier;

public interface IRegistry extends IIdentifiable {
    public void registerObject(Identifier key, Object value);
    
    public default void registerObject(IIdentifiable value) {
        registerObject(value.getIdentifier(), value);
    }
}