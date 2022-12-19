package sh.emberj.annotate.networking.mixins;

import java.util.concurrent.atomic.AtomicBoolean;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import sh.emberj.annotate.networking.ServerValidatorRegistry;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {

    @Shadow
    public abstract void acceptPlayer();

    @Shadow
    public abstract void disconnect(Text reason);

    @Shadow
    @Final
    public ClientConnection connection;

    @Unique
    private boolean _hasSentInfo;
    @Unique
    private AtomicBoolean _hasReceivedInfo;

    @Inject(method="<init>", at=@At("TAIL"))
    public void init(MinecraftServer server, ClientConnection connection, CallbackInfo info) {
        _hasReceivedInfo = new AtomicBoolean();
        _hasSentInfo = false;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginNetworkHandler;acceptPlayer()V"))
    public void tick(ServerLoginNetworkHandler handler) {
        if (!_hasSentInfo) {
            connection.send(ServerValidatorRegistry.INSTANCE.createValidationPacket());
            _hasSentInfo = true;
        }

        if (_hasReceivedInfo.get()) {
            acceptPlayer();
        }
    }

    @Inject(method = "onQueryResponse", at = @At("HEAD"), cancellable = true)
    private void onQueryResponse(LoginQueryResponseC2SPacket packet, CallbackInfo ci) {
        if (packet.getQueryId() == ServerValidatorRegistry.ANNOTATE_QUERY_ID) {
            ci.cancel();
            PacketByteBuf response = packet.getResponse();
            if (response == null) {
                disconnect(Text.of("You need the Annotate mod to join this server!"));
                return;
            }
            if (response.readBoolean()) {
                _hasReceivedInfo.set(true);
                return;
            } else {
                disconnect(
                        Text.of("Annotate detected a packet ID desync, probably caused by an incompatiable mod list."));
                return;
            }
        }
    }
}
