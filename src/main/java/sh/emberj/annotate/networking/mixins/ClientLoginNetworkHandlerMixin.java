package sh.emberj.annotate.networking.mixins;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.text.Text;
import sh.emberj.annotate.networking.ServerValidatorRegistry;

@Mixin(value = ClientLoginNetworkHandler.class, priority = 2000)
public abstract class ClientLoginNetworkHandlerMixin {

    @Shadow
    @Final
    public ClientConnection connection;

    @Shadow
    public abstract void onDisconnected(Text reason);

    @Unique
    private AtomicBoolean _hasReceivedInfo;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(ClientConnection connection, MinecraftClient client, Screen parentGui,
            Consumer<Text> statusConsumer, CallbackInfo info) {
        _hasReceivedInfo = new AtomicBoolean(false);
    }

    @Inject(method = "onQueryRequest", at = @At("HEAD"), cancellable = true)
    private void onQueryRequest(LoginQueryRequestS2CPacket packet, CallbackInfo info) {
        if (packet.getChannel().equals(ServerValidatorRegistry.ANNOTATE_CHANNEL)) {
            info.cancel();

            boolean validated = ServerValidatorRegistry.INSTANCE.verifyValidationPacket(packet.getPayload());

            if (validated) {
                ServerValidatorRegistry.LOG.info("Server validated.");
                _hasReceivedInfo.set(true);
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeBoolean(true);
                this.connection.send(new LoginQueryResponseC2SPacket(packet.getQueryId(), buf));
            } else {
                ServerValidatorRegistry.LOG.error("Server not valid.");
                this.connection.disconnect(
                        Text.of("Annotate detected a desync with the server, probably caused by an incompatiable mod list or different mod versions."));
                return;
            }

        }
    }

    @Inject(method = "onSuccess", at = @At("HEAD"), cancellable = true)
    public void onSuccess(LoginSuccessS2CPacket packet, CallbackInfo info) {
        if (!_hasReceivedInfo.get()) {
            this.connection.disconnect(Text.of("Cannot connect to server! Server not running the Annotate mod."));
            info.cancel();
        }
    }
}
