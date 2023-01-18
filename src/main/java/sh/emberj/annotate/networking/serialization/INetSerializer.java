package sh.emberj.annotate.networking.serialization;

import net.minecraft.network.PacketByteBuf;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.registry.IIdentifiable;

public interface INetSerializer extends IIdentifiable {

    public boolean trySerialize(Object object, Class<?> objectClass, PacketByteBuf buf) throws AnnotateException;
    public <T> T tryDeserialize(Class<T> objectClass, PacketByteBuf buf) throws AnnotateException;
    
    public default boolean shouldCache() {
        return true;
    }

    public default int getPriority() {
        return 0;
    }
    
}
