package sh.emberj.annotate.networking.callback;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayerEntity;
import sh.emberj.annotate.core.AnnotateException;

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
    public static <T> void executeServerbound(ServerboundCallback<T> callback, T param) {
        NetCallbackInfo info = getCallbackInfo(callback, NetworkSide.SERVERBOUND);   
        
    }

    public static <T> void executeClientbound(ClientConnection client, ClientboundCallback<T> callback, T param) {
        NetCallbackInfo info = getCallbackInfo(callback, NetworkSide.CLIENTBOUND);
    }

    public static <T> void executeClientbound(ServerPlayerEntity player, ClientboundCallback<T> callback, T param) {
        executeClientbound(player.networkHandler.connection, callback, param);
    }

    @Environment(EnvType.CLIENT)
    public static <T> void executeServerbound(ParamOnly<T> callback, T param) {
        executeServerbound((ctx, p) -> callback.invoke(p), param);
    }

    public static <T> void executeClientbound(ServerPlayerEntity player, ParamOnly<T> callback, T param) {
        executeClientbound(player, (ctx, p) -> callback.invoke(p), param);
    }

    public static <T> void executeClientbound(ClientConnection client, ParamOnly<T> callback, T param) {
        executeClientbound(client, (ctx, p) -> callback.invoke(p), param);
    }

    @Environment(EnvType.CLIENT)
    public static void executeServerbound(ServerboundCtxOnly callback) {
        executeServerbound((ctx, p) -> callback.invoke(ctx), null);
    }

    public static void executeClientbound(ServerPlayerEntity player, ClientboundCtxOnly callback) {
        executeClientbound(player, (ctx, p) -> callback.invoke(ctx), null);
    }

    public static void executeClientbound(ClientConnection client, ClientboundCtxOnly callback) {
        executeClientbound(client, (ctx, p) -> callback.invoke(ctx), null);
    }

    @Environment(EnvType.CLIENT)
    public static <T> void executeServerbound(Neither callback) {
        executeServerbound((ctx, p) -> callback.invoke(), null);
    }

    public static <T> void executeClientbound(ServerPlayerEntity player, Neither callback) {
        executeClientbound(player, (ctx, p) -> callback.invoke(), null);
    }

    public static <T> void executeClientbound(ClientConnection client, Neither callback) {
        executeClientbound(client, (ctx, p) -> callback.invoke(), null);
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
    @Environment(EnvType.CLIENT)
    public interface ServerboundCtxOnly {
        public void invoke(ServerboundCallbackContext ctx);
    }

    @FunctionalInterface
    public interface ClientboundCallback<T> extends AmbiguousCallback<ClientboundCallbackContext, T> {
    }

    @FunctionalInterface
    public interface ClientboundCtxOnly {
        public void invoke(ClientboundCallbackContext ctx);
    }

    @FunctionalInterface
    public interface ParamOnly<T> {
        public void invoke(T param);
    }

    @FunctionalInterface
    public interface Neither {
        public void invoke();
    }
}
