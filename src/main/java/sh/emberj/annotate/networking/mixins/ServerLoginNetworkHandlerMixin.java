package sh.emberj.annotate.networking.mixins;

import java.util.concurrent.atomic.AtomicBoolean;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import sh.emberj.annotate.networking.ServerValidatorRegistry;

@Mixin(value = ServerLoginNetworkHandler.class, priority = 2000)
public abstract class ServerLoginNetworkHandlerMixin {

    @Shadow
    public abstract void acceptPlayer();

    @Shadow
    public abstract void disconnect(Text reason);

    @Shadow
    @Final
    public ClientConnection connection;

    @Shadow
    public GameProfile profile;

    @Unique
    private boolean _hasSentInfo;
    @Unique
    private AtomicBoolean _hasReceivedInfo;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(MinecraftServer server, ClientConnection connection, CallbackInfo info) {
        _hasReceivedInfo = new AtomicBoolean();
        _hasSentInfo = false;
    }

    @Inject(method = "acceptPlayer", at = @At("HEAD"), cancellable = true)
    public void acceptPlayer(CallbackInfo info) {
        if (!_hasSentInfo) {
            connection.send(ServerValidatorRegistry.INSTANCE.createValidationPacket());
            _hasSentInfo = true;
        }

        if (!_hasReceivedInfo.get())
            info.cancel();
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
                ServerValidatorRegistry.LOG.info("Player " + profile.getName() + " validated.");
                _hasReceivedInfo.set(true);
                return;
            } else {
                disconnect(
                        Text.of("Serverbound verify packet sent with false. This message should never appear."));
                return;
            }
        }
    }
}
