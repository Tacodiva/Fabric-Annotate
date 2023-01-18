package sh.emberj.annotate.networking.serialization.serializers;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.networking.serialization.INetSerializer;
import sh.emberj.annotate.networking.serialization.NetSerializer;
import sh.emberj.annotate.networking.serialization.Nothing;

@NetSerializer
public class NothingSerializer implements INetSerializer {
    public static final Identifier ID = new Identifier(Annotate.ID, "nothing_serializer");

    @Override
    public boolean trySerialize(Object object, Class<?> objectClass, PacketByteBuf buf) throws AnnotateException {
        if (!objectClass.equals(Nothing.class))
            return false;
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T tryDeserialize(Class<T> objectClass, PacketByteBuf buf) throws AnnotateException {
        if (!objectClass.equals(Nothing.class))
            return null;
        return (T) Nothing.INSTANCE;
    }

    @Override
    public int getPriority() {
        return 1000;
    }
    
    @Override
    public Identifier getIdentifier() {
        return ID;
    }

}
