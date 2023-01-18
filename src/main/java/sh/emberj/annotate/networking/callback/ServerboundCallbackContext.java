package sh.emberj.annotate.networking.callback;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerboundCallbackContext {
        
    private final ServerPlayNetworkHandler _HANDLER;

    ServerboundCallbackContext(ServerPlayNetworkHandler handler) {
        _HANDLER = handler;
    }

    public ServerPlayNetworkHandler getNetworkHandler() {
        return _HANDLER;
    }

    public ServerPlayerEntity getPlayer() {
        return _HANDLER.getPlayer();
    }
}
