package sh.emberj.annotate.networking.serialization;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.Instance;
import sh.emberj.annotate.registry.GenericRegistry;
import sh.emberj.annotate.registry.Registry;

@Registry
public class NetSerializerRegistry extends GenericRegistry<INetSerializer> {

    public static final String ID = "annotate:net_serializer";
    @Instance
    public static final NetSerializerRegistry INSTANCE = new NetSerializerRegistry();

    private final SortedSet<INetSerializer> _REGISTRY;

    private final Map<Class<?>, INetSerializer> _TYPE_SERIALIZER_MAP;
    private final Map<Class<?>, INetSerializer> _TYPE_DESERIALIZER_MAP;

    private NetSerializerRegistry() {
        super(new Identifier(ID), INetSerializer.class);
        _REGISTRY = new TreeSet<>((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        _TYPE_SERIALIZER_MAP = new HashMap<>();
        _TYPE_DESERIALIZER_MAP = new HashMap<>();
    }

    @Override
    public void register(Identifier key, INetSerializer value) throws AnnotateException {  
        _REGISTRY.add(value);
    }

    public void serialize(Object object, PacketByteBuf buf) throws AnnotateException {
        if (object == null) throw new AnnotateException("Cannot serialize null.");
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
            if ((deserialized = cachedSerializer.tryDeserialize(objectClass, buf)) != null) {
                if (serializer.shouldCache())
                    _TYPE_DESERIALIZER_MAP.put(objectClass, serializer);
                return deserialized;
            }
        }

        throw new AnnotateException("No deserializer found for type " + objectClass + ".");
    }
}
