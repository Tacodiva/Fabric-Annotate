package sh.emberj.annotate.networking.serialization.serializers;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.network.PacketByteBuf;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.Instance;
import sh.emberj.annotate.networking.serialization.INetSerializeable;
import sh.emberj.annotate.networking.serialization.INetSerializer;
import sh.emberj.annotate.networking.serialization.NetSerializer;

@NetSerializer
public class NetSerializeableSerializer implements INetSerializer {

    @Instance
    public static final NetSerializeableSerializer INSTANCE = new NetSerializeableSerializer();

    private Map<Class<?>, Function<PacketByteBuf, ?>> _DESERIALIZER_CACHE;

    private NetSerializeableSerializer() {
        _DESERIALIZER_CACHE = new HashMap<>();
    }

    @Override
    public boolean trySerialize(Object object, Class<?> objectClass, PacketByteBuf buf) throws AnnotateException {
        if (!INetSerializeable.class.isAssignableFrom(objectClass))
            return false;
        ((INetSerializeable) object).write(buf);
        return true;
    }

    @Override
    public <T> T tryDeserialize(Class<T> objectClass, PacketByteBuf buf) throws AnnotateException {
        if (!INetSerializeable.class.isAssignableFrom(objectClass))
            return null;
        return getDeserializer(objectClass).apply(buf);
    }

    @SuppressWarnings("unchecked")
    public <T> Function<PacketByteBuf, T> getDeserializer(Class<T> clazz) throws AnnotateException {
        Function<PacketByteBuf, ?> deserializer = _DESERIALIZER_CACHE.get(clazz);
        if (deserializer != null)
            return (Function<PacketByteBuf, T>) deserializer;

        MethodHandles.Lookup lookup = MethodHandles.lookup();

        MethodHandle constructor;
        try {
            constructor = lookup.findConstructor(clazz, MethodType.methodType(void.class, PacketByteBuf.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new AnnotateException(
                    "Exception while finding constructor. Types which implement INetSerializeable must have a public constructor which takes in a PacketByteBuf as it's only parameter.",
                    e);
        }

        MethodType targetDescriptor = MethodType.methodType(clazz, String.class);
        String targetName = "apply";

        MethodType metafactoryDescriptor = MethodType.methodType(Function.class);

        CallSite metafactory;
        try {
            metafactory = LambdaMetafactory.metafactory(lookup, targetName, metafactoryDescriptor,
                    targetDescriptor.generic(), constructor, targetDescriptor);
        } catch (LambdaConversionException e) {
            throw new AnnotateException("Unexpected exception while creating lambda metafactory.", e);
        }

        try {
            deserializer = (Function<PacketByteBuf, Object>) metafactory.getTarget().invoke();
        } catch (Throwable e) {
            throw new AnnotateException("Unexpected exception while calling lambda metafactory.", e);
        }

        _DESERIALIZER_CACHE.put(clazz, deserializer);
        return (Function<PacketByteBuf, T>) deserializer;
    }

}
