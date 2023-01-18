package sh.emberj.annotate.networking.mcnative;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.AnnotatedClass;
import sh.emberj.annotate.registry.IIdentifiable;

public class NativePacketType<T extends PacketListener> implements IIdentifiable {

    private final Identifier _IDENTIFIER;

    private final Class<? extends Packet<T>> _PACKET_CLASS;
    private final AnnotatedClass _PACKET_TYPE;

    private final NetworkSide _SIDE;

    private int _id;

    public NativePacketType(Identifier identifier, NetworkSide side, AnnotatedClass packetType,
            Class<? extends Packet<T>> packetClass) {
        _IDENTIFIER = identifier;
        _PACKET_CLASS = packetClass;
        _PACKET_TYPE = packetType;
        _SIDE = side;
        _id = -1;
    }

    public boolean canHandle() {
        if (_SIDE == NetworkSide.CLIENTBOUND && FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
            return false;
        return true;
    }

    public Class<? extends Packet<T>> getPacketClass() {
        return _PACKET_CLASS;
    }

    public AnnotatedClass getPacketType() {
        return _PACKET_TYPE;
    }

    public NetworkSide getSide() {
        return _SIDE;
    }

    @Override
    public Identifier getIdentifier() {
        return _IDENTIFIER;
    }

    public int getID() {
        if (_id == -1)
            throw new IllegalStateException("A packet ID has not been assigned to " + getIdentifier() + " yet.");
        return _id;
    }

    void assignID(int id) {
        if (_id != -1)
            throw new IllegalStateException("A packet ID has already been assigned.");
        _id = id;
    }

}
