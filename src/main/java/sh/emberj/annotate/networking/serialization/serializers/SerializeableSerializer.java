package sh.emberj.annotate.networking.serialization.serializers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.io.serialization.ValidatingObjectInputStream;

import net.minecraft.network.PacketByteBuf;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.networking.serialization.INetSerializer;
import sh.emberj.annotate.networking.serialization.NetSerializer;

@NetSerializer
public class SerializeableSerializer implements INetSerializer {

    @Override
    public boolean trySerialize(Object object, Class<?> objectClass, PacketByteBuf buf) throws AnnotateException {
        if (!Serializable.class.isAssignableFrom(objectClass))
            return false;

        try {
            ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(outBytes);
            stream.writeObject(object);
            stream.close();
            buf.writeVarInt(outBytes.size());
            buf.writeBytes(outBytes.toByteArray());
            return true;
        } catch (IOException e) {
            throw new AnnotateException("Error serializing Serializable object.", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T tryDeserialize(Class<T> objectClass, PacketByteBuf buf) throws AnnotateException {
        if (!Serializable.class.isAssignableFrom(objectClass))
            return null;

        try {
            int length = buf.readVarInt();
            ByteArrayInputStream inBytes = new ByteArrayInputStream(buf.readByteArray(length));
            ValidatingObjectInputStream stream = new ValidatingObjectInputStream(inBytes);
            stream.accept(objectClass);
            return (T) stream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new AnnotateException("Error deserializing Serializable object.", e);
        }
    }

    @Override
    public int getPriority() {
        return 100;
    }

}
