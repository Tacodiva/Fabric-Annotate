package sh.emberj.annotate.networking.mcnative;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState.PacketHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.Instance;
import sh.emberj.annotate.networking.IServerValidator;
import sh.emberj.annotate.networking.ServerValidatorRegistry;
import sh.emberj.annotate.networking.serialization.serializers.NetSerializeableSerializer;
import sh.emberj.annotate.registry.FreezableRegistry;
import sh.emberj.annotate.registry.Register;
import sh.emberj.annotate.registry.Registry;

@Registry
@Register(registry = ServerValidatorRegistry.ID)
public class NativePacketRegistry extends FreezableRegistry<NativePacketType<?>> implements IServerValidator {
    public static final Logger LOG = LoggerFactory.getLogger("Annotate/NativePackets");

    @Instance
    public static final NativePacketRegistry INSTANCE = new NativePacketRegistry();

    public static final String ID = "annotate:net_native_packet";

    private Map<Identifier, NativePacketType<ClientPlayNetworkHandler>> _CLIENTBOUND_PACKET_MAP;
    private Map<Identifier, NativePacketType<ServerPlayNetworkHandler>> _SERVERBOUND_PACKET_MAP;

    private List<NativePacketType<ClientPlayNetworkHandler>> _clientboundPackets;
    private List<NativePacketType<ServerPlayNetworkHandler>> _serverboundPackets;

    @SuppressWarnings("unchecked")
    private NativePacketRegistry() {
        // The fact this cast is neccessary just shows how shit Java generics are
        super(new Identifier(ID), (Class<NativePacketType<?>>) (Object) NativePacketType.class);
        _CLIENTBOUND_PACKET_MAP = new HashMap<>();
        _SERVERBOUND_PACKET_MAP = new HashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void register(Identifier key, NativePacketType<?> value) {
        ensureNotFrozen();
        if (value.getSide() == NetworkSide.SERVERBOUND) {
            _SERVERBOUND_PACKET_MAP.put(key, (NativePacketType<ServerPlayNetworkHandler>) value);
        } else {
            _CLIENTBOUND_PACKET_MAP.put(key, (NativePacketType<ClientPlayNetworkHandler>) value);
        }
    }

    public void freeze(PacketHandler<ServerPlayNetworkHandler> serverboundHandler,
            PacketHandler<ClientPlayNetworkHandler> clientboundHandler) throws AnnotateException {
        super.freeze();

        _clientboundPackets = _CLIENTBOUND_PACKET_MAP.values().stream()
                .sorted((a, b) -> a.getIdentifier().compareTo(b.getIdentifier())).toList();
        _serverboundPackets = _SERVERBOUND_PACKET_MAP.values().stream()
                .sorted((a, b) -> a.getIdentifier().compareTo(b.getIdentifier())).toList();

        freeze(clientboundHandler, _clientboundPackets);
        freeze(serverboundHandler, _serverboundPackets);

        LOG.info("Injected " + _clientboundPackets.size() + " clientbound and " + _serverboundPackets.size()
                + " serverbound packets.");
    }

    private <T extends PacketListener> void freeze(PacketHandler<T> handler, List<NativePacketType<T>> packets)
            throws AnnotateException {
        final int baseID = handler.packetFactories.size();

        for (int i = 0; i < packets.size(); i++) {
            final NativePacketType<T> packet = packets.get(i);
            final int id = baseID + i;
            handler.packetIds.put(packet.getPacketClass(), id);
            packet.assignID(id);
            try {
                handler.packetFactories
                        .add(NetSerializeableSerializer.INSTANCE.getDeserializer(packet.getPacketClass()));
            } catch (AnnotateException e) {
                e.trySet(packet.getPacketType());
                throw e;
            }
        }
    }

    @Override
    public void writeValidationData(PacketByteBuf buf) {
        ensureFrozen();
        writeValidationData(buf, _CLIENTBOUND_PACKET_MAP.values());
        writeValidationData(buf, _SERVERBOUND_PACKET_MAP.values());
    }

    private <T extends PacketListener> void writeValidationData(PacketByteBuf buf,
            Collection<NativePacketType<T>> packets) {
        buf.writeVarInt(packets.size());
        for (NativePacketType<?> packet : packets) {
            buf.writeIdentifier(packet.getIdentifier());
            buf.writeInt(packet.getID());
        }
    }

    @Override
    public boolean validateData(PacketByteBuf buf) {
        ensureFrozen();
        if (!validateData(buf, _CLIENTBOUND_PACKET_MAP))
            return false;
        if (!validateData(buf, _SERVERBOUND_PACKET_MAP))
            return false;
        return true;
    }

    private <T extends PacketListener> boolean validateData(PacketByteBuf buf,
            Map<Identifier, NativePacketType<T>> packets) {
        int size = buf.readVarInt();
        if (size != packets.size())
            return false;
        for (int i = 0; i < size; i++) {
            NativePacketType<T> packet = packets.get(buf.readIdentifier());
            if (packet == null)
                return false;
            if (packet.getID() != buf.readInt())
                return false;
        }
        return true;
    }
}
