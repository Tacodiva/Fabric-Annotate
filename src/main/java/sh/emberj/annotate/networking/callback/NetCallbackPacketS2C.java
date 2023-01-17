package sh.emberj.annotate.networking.callback;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.networking.mcnative.INativeClientboundPacket;
import sh.emberj.annotate.networking.mcnative.NativePacket;
import sh.emberj.annotate.networking.serialization.NetSerializerRegistry;

@NativePacket
public class NetCallbackPacketS2C implements INativeClientboundPacket {

    public NetCallbackInfo info;
    public Object parameter;

    public NetCallbackPacketS2C(NetCallbackInfo info, Object parameter) {
        this.info = info;
        this.parameter = parameter;
    }

    public NetCallbackPacketS2C(PacketByteBuf buf) throws AnnotateException {
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
    public void apply(ClientPlayNetworkHandler ctx) {
        if (info.isExecutedAsynchronously()) {
            runCallback(ctx);
        } else {
            MinecraftClient.getInstance().execute(() -> runCallback(ctx));
        }
    }

    private void runCallback(ClientPlayNetworkHandler ctx) {
        info.invoke(new ClientboundCallbackContext(ctx), parameter);
    }
}
