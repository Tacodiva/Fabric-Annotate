package sh.emberj.annotate.networking.mcnative;

import net.minecraft.network.NetworkSide;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateIdentifier;
import sh.emberj.annotate.core.AnnotateScan;
import sh.emberj.annotate.core.AnnotatedType;
import sh.emberj.annotate.core.AnnotatedTypeHandler;

@AnnotateScan
public class NativePacketTypeHandler extends AnnotatedTypeHandler {

    @Override
    public void handle(AnnotatedType type) throws AnnotateException {
        NativePacket annotation = tryGetAnnotation(type, NativePacket.class);
        if (annotation == null)
            return;

        boolean isClientbound = INativeClientboundPacket.class.isAssignableFrom(type.getAsClass());
        boolean isServerbound = INativeServerboundPacket.class.isAssignableFrom(type.getAsClass());

        if (!isClientbound && !isServerbound) {
            throw new AnnotateException(
                    "Types annotated with @NativePacket must impliment INativeClientboundPacket or INativeServerboundPacket.");
        }

        Identifier id = AnnotateIdentifier.createIdentifier(annotation.path(), annotation.namespace(), type);

        if (isClientbound) {
            NativePacketRegistry.INSTANCE.register(id,
                    new NativePacketType<>(id, NetworkSide.CLIENTBOUND, type, INativeClientboundPacket.class));
        }

        if (isServerbound) {
            NativePacketRegistry.INSTANCE.register(id,
                    new NativePacketType<>(id, NetworkSide.SERVERBOUND, type, INativeServerboundPacket.class));
        }
    }

}
