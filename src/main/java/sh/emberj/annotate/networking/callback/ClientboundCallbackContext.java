package sh.emberj.annotate.networking.callback;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;

public class ClientboundCallbackContext {
    
    private final ClientPlayNetworkHandler _HANDLER;

    ClientboundCallbackContext(ClientPlayNetworkHandler handler) {
        _HANDLER = handler;
    }

    public ClientPlayNetworkHandler getNetworkHandler() {
        return _HANDLER;
    }

    public ClientWorld getWorld() {
        return _HANDLER.getWorld();
    }

    public MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }
}
