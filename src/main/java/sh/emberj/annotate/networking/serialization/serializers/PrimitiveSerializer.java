package sh.emberj.annotate.networking.serialization.serializers;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.networking.serialization.INetSerializer;
import sh.emberj.annotate.networking.serialization.NetSerializer;

@NetSerializer
public class PrimitiveSerializer implements INetSerializer {
    public static final Identifier ID = new Identifier(Annotate.ID, "primitive_serializer");

    @Override
    public boolean trySerialize(Object object, Class<?> objectClass, PacketByteBuf buf) throws AnnotateException {
        if (objectClass == Boolean.class)
            buf.writeBoolean((boolean) object);
        else if (objectClass == Character.class)
            buf.writeChar((char) object);
        else if (objectClass == Byte.class)
            buf.writeByte((byte) object);
        else if (objectClass == Short.class)
            buf.writeShort((short) object);
        else if (objectClass == Integer.class)
            buf.writeInt((int) object);
        else if (objectClass == Float.class)
            buf.writeFloat((float) object);
        else if (objectClass == Long.class)
            buf.writeLong((long) object);
        else if (objectClass == Double.class)
            buf.writeDouble((double) object);
        else if (objectClass == String.class)
            buf.writeString((String) object);
        else if (objectClass == Identifier.class)
            buf.writeIdentifier((Identifier) object);
        else if (objectClass == NbtCompound.class)
            buf.writeNbt((NbtCompound) object);
        else if (objectClass == PacketByteBuf.class) {
            buf.writeVarInt(buf.capacity());
            buf.writeBytes(buf);
        } else if (objectClass == BlockPos.class)
            buf.writeBlockPos((BlockPos) object);
        else
            return false;
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T tryDeserialize(Class<T> objectClass, PacketByteBuf buf) throws AnnotateException {
        // Safety first!
        if (objectClass == Boolean.class)
            return (T) (Boolean) buf.readBoolean();
        else if (objectClass == Character.class)
            return (T) (Character) buf.readChar();
        else if (objectClass == Byte.class)
            return (T) (Byte) buf.readByte();
        else if (objectClass == Short.class)
            return (T) (Short) buf.readShort();
        else if (objectClass == Integer.class)
            return (T) (Integer) buf.readInt();
        else if (objectClass == Float.class)
            return (T) (Float) buf.readFloat();
        else if (objectClass == Long.class)
            return (T) (Long) buf.readLong();
        else if (objectClass == Double.class)
            return (T) (Double) buf.readDouble();
        else if (objectClass == String.class)
            return (T) buf.readString();
        else if (objectClass == Identifier.class)
            return (T) buf.readIdentifier();
        else if (objectClass == NbtCompound.class)
            return (T) buf.readNbt();
        else if (objectClass == PacketByteBuf.class)
            return (T) buf.readBytes(buf.readVarInt());
        else if (objectClass == BlockPos.class)
            return (T) buf.readBlockPos();
        else
            return null;
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
