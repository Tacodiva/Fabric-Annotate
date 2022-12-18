package sh.emberj.annotate.networking.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.NetworkState.PacketHandler;
import net.minecraft.network.NetworkState.PacketHandlerInitializer;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import sh.emberj.annotate.networking.AnnotateNetClient;
import sh.emberj.annotate.networking.AnnotateNetServer;
import sh.emberj.annotate.networking.mcnative.NativePacketSide;

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

            PacketHandler<ServerPlayNetworkHandler> serverbound = (PacketHandler<ServerPlayNetworkHandler>) initializer.packetHandlers
                    .get(NetworkSide.SERVERBOUND);
            AnnotateNetServer.getServerboundSide().assignIDs(serverbound);
            AnnotateNetServer.getServerboundSide().createFactories(serverbound);

            PacketHandler<?> clientbound = initializer.packetHandlers.get(NetworkSide.CLIENTBOUND);
            AnnotateNetServer.getClientboundSide().assignIDs(clientbound);
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
                AnnotateNetClient.getClientboundSide()
                        .createFactories((PacketHandler<ClientPlayNetworkHandler>) clientbound);

        }
    }

}
