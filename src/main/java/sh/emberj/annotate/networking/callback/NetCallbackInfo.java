package sh.emberj.annotate.networking.callback;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.objectweb.asm.Type;

import net.minecraft.network.NetworkSide;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.Utils;
import sh.emberj.annotate.core.asm.MethodMetadata;
import sh.emberj.annotate.networking.callback.NetworkCallbacks.AmbiguousCallback;
import sh.emberj.annotate.registry.IIdentifiable;

public class NetCallbackInfo implements IIdentifiable {

    private static final Class<?> CLASS_CTX_CLIENTBOUND = ClientboundCallbackContext.class;
    private static final Class<?> CLASS_CTX_SERVERBOUND = ServerboundCallbackContext.class;

    private static final Type TYPE_CTX_CLIENTBOUND = Type.getType(CLASS_CTX_CLIENTBOUND);
    private static final Type TYPE_CTX_SERVERBOUND = Type.getType(CLASS_CTX_SERVERBOUND);

    private static Type getContextType(NetworkSide side) {
        if (side == NetworkSide.CLIENTBOUND)
            return TYPE_CTX_CLIENTBOUND;
        if (side == NetworkSide.SERVERBOUND)
            return TYPE_CTX_SERVERBOUND;
        return null;
    }

    private static Class<?> getContextClass(NetworkSide side) {
        if (side == NetworkSide.CLIENTBOUND)
            return CLASS_CTX_CLIENTBOUND;
        if (side == NetworkSide.SERVERBOUND)
            return CLASS_CTX_SERVERBOUND;
        return null;
    }

    private static AmbiguousCallback<Object, Object> createCallback(NetCallbackInfo info) throws AnnotateException {
        MethodMetadata meta = info.getMethodMeta();
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        MethodType targetDescriptor = MethodType.methodType(void.class, getContextClass(info.getSide()),
                info.getParameterArgClass());

        MethodHandle method;
        try {
            method = lookup.findStatic(meta.getDeclaringClass().getAsClass(), meta.getName(), targetDescriptor);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new AnnotateException("Exception while finding callback method handle.", e);
        }

        String targetName = "invoke";
        MethodType metafactoryDescriptor = MethodType.methodType(AmbiguousCallback.class);

        CallSite metafactory;
        try {
            metafactory = LambdaMetafactory.metafactory(lookup, targetName, metafactoryDescriptor,
                    targetDescriptor.generic().changeReturnType(void.class), method, targetDescriptor);
        } catch (LambdaConversionException e) {
            throw new AnnotateException("Unexpected exception while creating lambda metafactory.", e);
        }

        try {
            return (AmbiguousCallback<Object, Object>) metafactory.getTarget().invoke();
        } catch (Throwable e) {
            throw new AnnotateException("Unexpected exception while calling lambda metafactory.", e);
        }
    }

    private final MethodMetadata _METHOD_META;
    private final Identifier _ID;
    private final NetworkSide _SIDE;
    private final boolean _EXECUTE_ASYNC;

    private AmbiguousCallback<Object, Object> _callback;

    private final Class<?> _ARG_PARAM_CLASS;

    public NetCallbackInfo(Identifier id, NetworkSide side, boolean executeAsync, AnnotatedMethod method)
            throws AnnotateException {
        _METHOD_META = method.getMetadata();
        _ID = id;
        _SIDE = side;
        _EXECUTE_ASYNC = executeAsync;

        Type[] methodArgs = _METHOD_META.getArgTypes();

        if (methodArgs.length != 2)
            throw new AnnotateException("Wrong number of arguments on network callback. Expected 2.", method);

        _ARG_PARAM_CLASS = Utils.loadClass(methodArgs[1]);
        if (!methodArgs[0].equals(getContextType(side)))
            throw new AnnotateException("Wrong first argument on network callback. Exepected "
                    + getContextType(side) + " but found " + methodArgs[0] + ".", method);
    }

    public MethodMetadata getMethodMeta() {
        return _METHOD_META;
    }

    public NetworkSide getSide() {
        return _SIDE;
    }

    public Class<?> getParameterArgClass() {
        return _ARG_PARAM_CLASS;
    }

    public boolean isExecutedAsynchronously() {
        return _EXECUTE_ASYNC;
    }

    @Override
    public Identifier getIdentifier() {
        return _ID;
    }

    public void invoke(Object context, Object parameter) {
        try {
            if (_callback == null)
                _callback = createCallback(this);
            _callback.invoke(context, parameter);
        } catch (AnnotateException e) {
            throw new RuntimeException(e);
        }
    }
}
