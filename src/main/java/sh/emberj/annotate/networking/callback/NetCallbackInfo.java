package sh.emberj.annotate.networking.callback;

import org.objectweb.asm.Type;

import net.minecraft.network.NetworkSide;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.asm.AnnotatedMethodMeta;
import sh.emberj.annotate.registry.IIdentifiable;

public class NetCallbackInfo implements IIdentifiable {

    private static final Type TYPE_CTX_CLIENTBOUND = Type.getType(ClientboundCallbackContext.class);
    private static final Type TYPE_CTX_SERVERBOUND = Type.getType(ServerboundCallbackContext.class);

    private static Type getContextType(NetworkSide side) {
        if (side == NetworkSide.CLIENTBOUND)
            return TYPE_CTX_CLIENTBOUND;
        if (side == NetworkSide.SERVERBOUND)
            return TYPE_CTX_SERVERBOUND;
        return null;
    }

    private final AnnotatedMethodMeta _METHOD_META;
    private final Identifier _ID;
    private final NetworkSide _SIDE;
    private final boolean _EXECUTE_ASYNC;

    private final Class<?> _ARG_PARAM_CLASS;
    private final boolean _HAS_ARG_CONTEXT;

    public NetCallbackInfo(Identifier id, NetworkSide side, boolean executeAsync, AnnotatedMethod method)
            throws AnnotateException {
        _METHOD_META = method.getMeta();
        _ID = id;
        _SIDE = side;
        _EXECUTE_ASYNC = executeAsync;

        Type[] methodArgs = _METHOD_META.getArgTypes();

        if (methodArgs.length > 2)
            throw new AnnotateException("Too many arguments on network callback. Expected a maximum of 2.", method);

        if (methodArgs.length == 2) {
            _HAS_ARG_CONTEXT = true;
            _ARG_PARAM_CLASS = methodArgs[1].getClass();
            if (!methodArgs[0].equals(getContextType(side)))
                throw new AnnotateException("Wrong first argument on network callback. Exepected "
                        + getContextType(side) + " but found " + methodArgs[0] + ".", method);
        } else if (methodArgs.length == 1) {
            if (methodArgs[0].equals(getContextType(side))) {
                _ARG_PARAM_CLASS = null;
                _HAS_ARG_CONTEXT = true;
            } else {
                _ARG_PARAM_CLASS = methodArgs[0].getClass();
                _HAS_ARG_CONTEXT = false;
            }
        } else {
            _ARG_PARAM_CLASS = null;
            _HAS_ARG_CONTEXT = false;
        }
    }

    public AnnotatedMethodMeta getMethodMeta() {
        return _METHOD_META;
    }

    public NetworkSide getSide() {
        return _SIDE;
    }

    public Class<?> getParameterArgClass() {
        return _ARG_PARAM_CLASS;
    }

    public boolean hasParameterArg() {
        return _ARG_PARAM_CLASS != null;
    }

    public boolean hasContextArg() {
        return _HAS_ARG_CONTEXT;
    }

    public boolean isExecutedAsynchronously() {
        return _EXECUTE_ASYNC;
    }

    @Override
    public Identifier getIdentifier() {
        return _ID;
    }
}
