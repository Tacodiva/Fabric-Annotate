package sh.emberj.annotate.networking.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.NetworkState.PacketHandler;
import net.minecraft.network.NetworkState.PacketHandlerInitializer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.networking.ServerValidatorRegistry;
import sh.emberj.annotate.networking.mcnative.NativePacketRegistry;

@Mixin(NetworkState.class)
@SuppressWarnings("unused")
public class NetworkStateMixin {
    private final static int HANDSHAKING_ID = -1;
    private final static int PLAY_ID = 0;
    private final static int STATUS_ID = 1;
    private final static int LOGIN_ID = 2;

    @Inject(at = @At("TAIL"), method = "<init>")
    @SuppressWarnings("unchecked")
    private void injectPackets(String name, int index, int id, PacketHandlerInitializer initializer,
            CallbackInfo info) {
        if (id == PLAY_ID) {
            PacketHandler<ServerPlayNetworkHandler> serverboundHandler = (PacketHandler<ServerPlayNetworkHandler>) initializer.packetHandlers
                    .get(NetworkSide.SERVERBOUND);
            PacketHandler<ClientPlayNetworkHandler> clientboundHandler = (PacketHandler<ClientPlayNetworkHandler>) initializer.packetHandlers
                    .get(NetworkSide.CLIENTBOUND);
            try {
                NativePacketRegistry.INSTANCE.freeze(serverboundHandler, clientboundHandler);
            } catch (AnnotateException e) {
                throw new RuntimeException(e);
            }
            ServerValidatorRegistry.INSTANCE.freeze();
        }
    }

}
