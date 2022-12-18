package sh.emberj.annotate.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.networking.mcnative.NativePacketSide;
import sh.emberj.annotate.networking.mcnative.NativePacketType;

@Environment(EnvType.CLIENT)
public class AnnotateNetClient {

    private static final NativePacketSide<ClientPlayNetworkHandler> _CLIENTBOUND_PACKETS;

    static {
        _CLIENTBOUND_PACKETS = new NativePacketSide<>();
    }

    public static NativePacketSide<ClientPlayNetworkHandler> getClientboundSide() {
        return _CLIENTBOUND_PACKETS;
    }

    public static NativePacketType<ClientPlayNetworkHandler> registerNativeClientboundPacket(Identifier id) {
        return _CLIENTBOUND_PACKETS.registerNew(id);
    }

    @SuppressWarnings("resource")
    public static void sendNativeServerbound(Identifier packet, PacketByteBuf data) {
        MinecraftClient.getInstance().player.networkHandler.sendPacket(_CLIENTBOUND_PACKETS.createPacket(packet, data));
    }

}
