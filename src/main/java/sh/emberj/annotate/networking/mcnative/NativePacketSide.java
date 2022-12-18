package sh.emberj.annotate.networking.mcnative;

import java.util.HashMap;
import java.util.function.Function;

import net.minecraft.network.NetworkState.PacketHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.Identifier;

public class NativePacketSide<T extends PacketListener> {
    private NativePacketType<T>[] _packets;
    private final HashMap<Identifier, NativePacketType<T>> _PACKET_MAP;
    private boolean _frozen;

    public NativePacketSide() {
        _PACKET_MAP = new HashMap<>();
        _frozen = false;
    }

    public void freeze() {
        _frozen = true;
    }

    private void checkNotFrozen() {
        if (_frozen)
            throw new IllegalStateException("Packet IDs have already been assigned.");
    }

    public NativePacketType<T> registerNew(Identifier identifier) {
        checkNotFrozen();
        NativePacketType<T> type = new NativePacketType<>(identifier);
        _PACKET_MAP.put(identifier, type);
        return type;
    }

    public Packet<?> createPacket(Identifier type, PacketByteBuf buf) {
        return new NativePacket(_PACKET_MAP.get(type), buf);
    }

    @SuppressWarnings("unchecked") // This is a stupid warning. This is clearly safe please stfu java compiler
    private Class<? extends Packet<T>> getPacketClass() {
        return (Class<? extends Packet<T>>) NativePacket.class;
    }

    @SuppressWarnings("unchecked") // This is actually unsafe, but it doesn't matter
    public void assignIDs(PacketHandler<?> handler) {
        checkNotFrozen();
        _frozen = true;

        _packets = _PACKET_MAP.values().stream().sorted((a, b) -> a.getIdentifier().compareTo(b.getIdentifier()))
                .toArray(NativePacketType[]::new);

        final int baseId = handler.packetFactories.size();
        for (int i = 0; i < _packets.length; i++) {
            int id = baseId + i;
            ((PacketHandler<T>) handler).packetIds.put(getPacketClass(), id);
        }
    }

    public void createFactories(PacketHandler<T> handler) {
        for (int i = 0; i < _packets.length; i++) {
            handler.packetFactories.add(new NativePacketFactory(_packets[i]));
        }
    }

    private class NativePacketFactory implements Function<PacketByteBuf, NativePacket> {
        private final NativePacketType<T> _TYPE;

        public NativePacketFactory(NativePacketType<T> type) {
            _TYPE = type;
        }

        @Override
        public NativePacket apply(PacketByteBuf buf) {
            return new NativePacket(_TYPE, buf);
        }
    }

    private class NativePacket implements Packet<T> {
        private final NativePacketType<T> _TYPE;
        private final PacketByteBuf _DATA;

        public NativePacket(NativePacketType<T> type, PacketByteBuf data) {
            _TYPE = type;
            _DATA = data;
        }

        @Override
        public void write(PacketByteBuf buf) {
            buf.writeBytes(_DATA);
        }

        @Override
        public void apply(T ctx) {
            _TYPE.handle(_DATA, ctx);
        }
    }
}