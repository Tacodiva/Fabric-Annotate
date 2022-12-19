package sh.emberj.annotate.networking.mcnative;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    }

    private <T extends PacketListener> void freeze(PacketHandler<T> handler, List<NativePacketType<T>> packets)
            throws AnnotateException {
        final int baseID = handler.packetFactories.size();

        for (int i = 0; i < packets.size(); i++) {
            final NativePacketType<T> packet = packets.get(i);
            final int id = baseID + i;
            handler.packetIds.put(packet.getPacketClass(), id);
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
    }

    @Override
    public boolean validateData(PacketByteBuf buf) {
        ensureFrozen();
        return true;
    }
}
