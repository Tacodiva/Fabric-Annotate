package sh.emberj.annotate.networking.mcnative;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import sh.emberj.annotate.networking.serialization.INetSerializeable;

public interface INativeClientboundPacket extends Packet<ClientPlayNetworkHandler>, INetSerializeable {
}