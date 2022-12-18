package sh.emberj.annotate.networking.mcnative;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;

public interface INativePacketHandler<T extends PacketListener> {
    public void handle(PacketByteBuf data, T ctx);
}
