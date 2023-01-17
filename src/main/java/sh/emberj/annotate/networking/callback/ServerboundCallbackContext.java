package sh.emberj.annotate.networking.callback;

import net.minecraft.server.network.ServerPlayerEntity;

public class ServerboundCallbackContext {
        
    private final ServerboundCallbackContext _HANDLER;

    ServerboundCallbackContext(ServerboundCallbackContext handler) {
        _HANDLER = handler;
    }

    public ServerboundCallbackContext getNetworkHandler() {
        return _HANDLER;
    }

    public ServerPlayerEntity getPlayer() {
        return _HANDLER.getPlayer();
    }
}
