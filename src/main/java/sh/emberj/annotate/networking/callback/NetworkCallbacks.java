package sh.emberj.annotate.networking.callback;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayerEntity;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.networking.serialization.Nothing;

public class NetworkCallbacks {
    private NetworkCallbacks() {
    }

    private static NetCallbackInfo getCallbackInfo(AmbiguousCallback<?, ?> callback, NetworkSide expectedSide) {
        try {
            NetCallbackInfo info = NetCallbackRegistry.INSTANCE.findCallbackInfo(callback);
            if (info.getSide() != expectedSide)
                throw new AnnotateException("Callback had wrong network side. Expected " + expectedSide + ".");
            return info;
        } catch (AnnotateException e) {
            throw new RuntimeException(e);
        }
    }

    @Environment(EnvType.CLIENT)
    @SuppressWarnings("resource")
    public static <T> void execute(ServerboundCallback<T> callback, T param) {
        NetCallbackInfo info = getCallbackInfo(callback, NetworkSide.SERVERBOUND);
        MinecraftClient.getInstance().player.networkHandler.getConnection().send(new NetCallbackPacketC2S(info, param));
    }

    public static <T> void execute(ClientConnection client, ClientboundCallback<T> callback, T param) {
        NetCallbackInfo info = getCallbackInfo(callback, NetworkSide.CLIENTBOUND);
        client.send(new NetCallbackPacketS2C(info, param));
    }

    public static <T> void execute(ServerPlayerEntity player, ClientboundCallback<T> callback, T param) {
        execute(player.networkHandler.connection, callback, param);
    }

    @Environment(EnvType.CLIENT)
    public static void execute(ServerboundCallback<Nothing> callback) {
        execute(callback, Nothing.INSTANCE);
    }

    public static void execute(ClientConnection client, ClientboundCallback<Nothing> callback) {
        execute(client, callback, Nothing.INSTANCE);
    }

    public static void execute(ServerPlayerEntity player, ClientboundCallback<Nothing> callback) {
        execute(player, callback, Nothing.INSTANCE);
    }

    @FunctionalInterface
    public interface AmbiguousCallback<TCtx, TParam> {
        public void invoke(TCtx ctx, TParam param);
    }

    @FunctionalInterface
    @Environment(EnvType.CLIENT)
    public interface ServerboundCallback<T> extends AmbiguousCallback<ServerboundCallbackContext, T> {
    }

    @FunctionalInterface
    public interface ClientboundCallback<T> extends AmbiguousCallback<ClientboundCallbackContext, T> {
    }
}
