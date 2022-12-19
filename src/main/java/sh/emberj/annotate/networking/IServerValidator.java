package sh.emberj.annotate.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import sh.emberj.annotate.registry.IIdentifiable;

public interface IServerValidator extends IIdentifiable {

    public void writeValidationData(PacketByteBuf buf);

    @Environment(EnvType.CLIENT)
    public boolean validateData(PacketByteBuf buf);

}
