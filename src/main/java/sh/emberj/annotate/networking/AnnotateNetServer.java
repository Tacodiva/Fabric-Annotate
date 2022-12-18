package sh.emberj.annotate.networking;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.networking.mcnative.NativePacketSide;
import sh.emberj.annotate.networking.mcnative.NativePacketType;

public class AnnotateNetServer {

    public static final Identifier ANNOTATE_CHANNEL = new Identifier("annotate", "main");
    public static final int ANNOTATE_QUERY_ID = 807380476;
    public static final byte VERSION_ID = 1;

    private static final Map<Identifier, IServerValidator> _VALIDATORS;

    private static final NativePacketSide<ServerPlayNetworkHandler> _SERVERBOUND_PACKETS;
    private static final NativePacketSide<?> _CLIENTBOUND_PACKETS;

    static {
        _VALIDATORS = new HashMap<>();
        _SERVERBOUND_PACKETS = new NativePacketSide<>();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            _CLIENTBOUND_PACKETS = AnnotateNetClient.getClientboundSide();
        } else {
            _CLIENTBOUND_PACKETS = new NativePacketSide<>();
        }
    }

    public static void sendNativeClientbound(ServerPlayerEntity player, Identifier packet, PacketByteBuf data) {
        player.networkHandler.connection.send(_CLIENTBOUND_PACKETS.createPacket(packet, data));
    }

    public static NativePacketType<ServerPlayNetworkHandler> registerNativeServerboundPacket(Identifier id) {
        return _SERVERBOUND_PACKETS.registerNew(id);
    }

    public static NativePacketType<?> registerNativeClientboundPacket(Identifier id) {
        return _CLIENTBOUND_PACKETS.registerNew(id);
    }

    public static NativePacketSide<ServerPlayNetworkHandler> getServerboundSide() {
        return _SERVERBOUND_PACKETS;
    }

    public static NativePacketSide<?> getClientboundSide() {
        return _CLIENTBOUND_PACKETS;
    }
    
    public static Packet<?> createValidationPacket() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeByte(VERSION_ID);
        buf.writeVarInt(_VALIDATORS.size());
        for (Entry<Identifier, IServerValidator> validator : _VALIDATORS.entrySet()) {
            buf.writeIdentifier(validator.getKey());
            validator.getValue().writeValidationData(buf);
        }

        return new LoginQueryRequestS2CPacket(ANNOTATE_QUERY_ID, ANNOTATE_CHANNEL, buf);
    }

    @Environment(EnvType.CLIENT)
    public static boolean verifyValidationPacket(PacketByteBuf buf) {
        if (buf.readByte() != VERSION_ID) {
            Annotate.LOG.info("Failed to validate server. Wrong version id.");
            return false;
        }

        int validatorCount = buf.readVarInt();

        if (validatorCount != _VALIDATORS.size())
            return false;

        for (int i = 0; i < validatorCount; i++) {
            Identifier validatorId = buf.readIdentifier();
            IServerValidator validator = _VALIDATORS.get(validatorId);
            if (validator == null)
                return false;
            if (!validator.validateData(buf))
                return false;
        }

        return true;
    }

    public static interface IServerValidator {

        public Identifier getIdentifier();

        public void writeValidationData(PacketByteBuf buf);

        @Environment(EnvType.CLIENT)
        public boolean validateData(PacketByteBuf buf);
    }

}
