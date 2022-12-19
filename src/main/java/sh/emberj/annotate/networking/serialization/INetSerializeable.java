package sh.emberj.annotate.networking.serialization;

import net.minecraft.network.PacketByteBuf;
import sh.emberj.annotate.core.AnnotateException;

/**
 * Implimentations of INetSerializeable must also have a public constructor
 * which has one argument of type PacketByteBuf
 */
public interface INetSerializeable {
    public void write(PacketByteBuf buf) throws AnnotateException;
}
