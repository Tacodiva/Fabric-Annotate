package sh.emberj.annotate.networking.serialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.Instance;
import sh.emberj.annotate.networking.IServerValidator;
import sh.emberj.annotate.networking.ServerValidatorRegistry;
import sh.emberj.annotate.registry.FreezableRegistry;
import sh.emberj.annotate.registry.Register;
import sh.emberj.annotate.registry.Registry;

@Registry
@Register(registry = ServerValidatorRegistry.ID)
public class NetSerializerRegistry extends FreezableRegistry<INetSerializer> implements IServerValidator {

    public static final String ID = "annotate:net_serializer";
    @Instance
    public static final NetSerializerRegistry INSTANCE = new NetSerializerRegistry();

    private final List<INetSerializer> _REGISTRY;

    private final Map<Class<?>, INetSerializer> _TYPE_SERIALIZER_MAP;
    private final Map<Class<?>, INetSerializer> _TYPE_DESERIALIZER_MAP;

    private NetSerializerRegistry() {
        super(new Identifier(ID), INetSerializer.class);
        _REGISTRY = new ArrayList<>();
        _TYPE_SERIALIZER_MAP = new HashMap<>();
        _TYPE_DESERIALIZER_MAP = new HashMap<>();
    }

    @Override
    public void register(Identifier key, INetSerializer value) throws AnnotateException {
        ensureNotFrozen();
        int index = Collections.binarySearch(_REGISTRY, value,
                Comparator.comparing(INetSerializer::getPriority).reversed());
        if (index < 0)
            index = -index - 1;
        _REGISTRY.add(index, value);
    }

    public void serialize(Object object, PacketByteBuf buf) throws AnnotateException {
        tryFreeze();
        if (object == null)
            throw new AnnotateException("Cannot serialize null.");
        final Class<?> objectClass = object.getClass();

        INetSerializer cachedSerializer = _TYPE_SERIALIZER_MAP.get(objectClass);

        if (cachedSerializer != null) {
            if (cachedSerializer.trySerialize(object, objectClass, buf)) {
                return;
            } else {
                Annotate.LOG.warn(
                        "Cached serializer " + cachedSerializer + " did not serialize type " + objectClass
                                + ". The serializer should override shouldCache() and make it return false.");
                _TYPE_SERIALIZER_MAP.remove(objectClass);
            }
        }

        for (INetSerializer serializer : _REGISTRY) {
            if (serializer.trySerialize(object, objectClass, buf)) {
                if (serializer.shouldCache())
                    _TYPE_SERIALIZER_MAP.put(objectClass, serializer);
                return;
            }
        }

        throw new AnnotateException("No serializer found for type " + objectClass + ".");
    }

    public <T> T deserialize(Class<T> objectClass, PacketByteBuf buf) throws AnnotateException {
        tryFreeze();
        INetSerializer cachedSerializer = _TYPE_DESERIALIZER_MAP.get(objectClass);
        T deserialized;

        if (cachedSerializer != null) {
            if ((deserialized = cachedSerializer.tryDeserialize(objectClass, buf)) != null) {
                return deserialized;
            } else {
                Annotate.LOG.warn(
                        "Cached deserializer " + cachedSerializer + " did not deserialize type " + objectClass
                                + ". The deserializer should override shouldCache() and make it return false.");
            }
        }

        for (INetSerializer serializer : _REGISTRY) {
            if ((deserialized = serializer.tryDeserialize(objectClass, buf)) != null) {
                if (serializer.shouldCache())
                    _TYPE_DESERIALIZER_MAP.put(objectClass, serializer);
                return deserialized;
            }
        }

        throw new AnnotateException("No deserializer found for type " + objectClass + ".");
    }

    @Override
    public void writeValidationData(PacketByteBuf buf) {
        buf.writeVarInt(_REGISTRY.size());
        for (INetSerializer serializer : _REGISTRY)
            buf.writeIdentifier(serializer.getIdentifier());
    }

    @Override
    public boolean validateData(PacketByteBuf buf) {
        int size = buf.readVarInt();
        if (size != _REGISTRY.size())
            return false;
        for (INetSerializer serializer : _REGISTRY) {
            if (!serializer.getIdentifier().equals(buf.readIdentifier()))
                return false;
        }
        return true;
    }
}
