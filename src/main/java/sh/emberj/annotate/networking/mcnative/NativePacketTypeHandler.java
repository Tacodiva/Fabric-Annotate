package sh.emberj.annotate.networking.mcnative;

import net.minecraft.network.NetworkSide;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateIdentifier;
import sh.emberj.annotate.core.AnnotatedClass;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.handled.IClassAnnotationHandler;

public class NativePacketTypeHandler implements IClassAnnotationHandler {

    @Override
    @SuppressWarnings("unchecked")
    public void handleClassAnnotation(AnnotatedClass class_, AnnotationMetadata annotation) throws AnnotateException {
        boolean isClientbound = INativeClientboundPacket.class.isAssignableFrom(class_.getMetadata().getAsClass());
        boolean isServerbound = INativeServerboundPacket.class.isAssignableFrom(class_.getMetadata().getAsClass());

        if (!isClientbound && !isServerbound) {
            throw new AnnotateException(
                    "Types annotated with @NativePacket must impliment INativeClientboundPacket or INativeServerboundPacket.");
        }

        Identifier id = AnnotateIdentifier.createIdentifier(annotation.getStringParam("path"),
                annotation.getStringParam("namespace"), class_);

        if (isClientbound) {
            NativePacketRegistry.INSTANCE.register(id,
                    new NativePacketType<>(id, NetworkSide.CLIENTBOUND, class_, (Class<? extends INativeClientboundPacket>) class_.getMetadata().getAsClass()));
        }

        if (isServerbound) {
            NativePacketRegistry.INSTANCE.register(id,
                    new NativePacketType<>(id, NetworkSide.SERVERBOUND, class_, (Class<? extends INativeServerboundPacket>) class_.getMetadata().getAsClass()));
        }
    }

}
