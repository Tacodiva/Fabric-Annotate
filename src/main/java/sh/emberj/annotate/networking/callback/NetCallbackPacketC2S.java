package sh.emberj.annotate.networking.callback;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import sh.emberj.annotate.networking.mcnative.INativeServerboundPacket;
import sh.emberj.annotate.networking.mcnative.NativePacket;

@NativePacket
public class NetCallbackPacketC2S implements INativeServerboundPacket {

    @Override
    public void write(PacketByteBuf var1) {

    }

    @Override
    public void apply(ServerPlayNetworkHandler var1) {
                
    }
    
}
