package sh.emberj.annotate.networking.mcnative;

import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import sh.emberj.annotate.networking.serialization.INetSerializeable;

public interface INativeServerboundPacket extends Packet<ServerPlayNetworkHandler>, INetSerializeable {

}