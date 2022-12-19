package sh.emberj.annotate.registry;

import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.AnnotateException;

public interface IRegistry extends IIdentifiable {
    public void registerObject(Identifier key, Object value) throws AnnotateException;
    
    public default void registerObject(IIdentifiable value) throws AnnotateException {
        registerObject(value.getIdentifier(), value);
    }
}