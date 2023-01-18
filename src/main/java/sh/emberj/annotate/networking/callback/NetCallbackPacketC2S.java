package sh.emberj.annotate.networking.callback;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.networking.mcnative.INativeServerboundPacket;
import sh.emberj.annotate.networking.mcnative.NativePacket;
import sh.emberj.annotate.networking.serialization.NetSerializerRegistry;

@NativePacket
public class NetCallbackPacketC2S implements INativeServerboundPacket {

    public NetCallbackInfo info;
    public Object parameter;

    public NetCallbackPacketC2S(NetCallbackInfo info, Object parameter) {
        this.info = info;
        this.parameter = parameter;
    }

    public NetCallbackPacketC2S(PacketByteBuf buf) throws AnnotateException {
        Identifier infoId = buf.readIdentifier();
        info = NetCallbackRegistry.INSTANCE.get(infoId);
        if (info == null)
            throw new AnnotateException("Invalid network callback id '" + infoId + "'.");
        parameter = NetSerializerRegistry.INSTANCE.deserialize(info.getParameterArgClass(), buf);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(info.getIdentifier());
        try {
            NetSerializerRegistry.INSTANCE.serialize(parameter, buf);
        } catch (AnnotateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void apply(ServerPlayNetworkHandler ctx) {
        if (info.isExecutedAsynchronously()) {
            runCallback(ctx);
        } else {
            ctx.getPlayer().getServer().execute(() -> runCallback(ctx));
        }
    }

    private void runCallback(ServerPlayNetworkHandler ctx) {
        info.invoke(new ServerboundCallbackContext(ctx), parameter);
    }
    
}
