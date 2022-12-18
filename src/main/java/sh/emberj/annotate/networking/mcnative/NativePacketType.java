package sh.emberj.annotate.networking.mcnative;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.Identifier;

public class NativePacketType<T extends PacketListener> {
    private INativePacketHandler<T> _handler;
    private final Identifier _ID;

    NativePacketType(Identifier id) {
        _ID = id;
    }

    public Identifier getIdentifier() {
        return _ID;
    }

    public void setHandler(INativePacketHandler<T> handler) {
        if (_handler != null)
            throw new IllegalStateException("Packet already has a handler.");
        _handler = handler;
    }

    public void handle(PacketByteBuf data, T ctx) {
        if (_handler == null)
            throw new IllegalStateException("No packet handler set.");
        _handler.handle(data, ctx);
    }
}
