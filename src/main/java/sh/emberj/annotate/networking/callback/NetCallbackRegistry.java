package sh.emberj.annotate.networking.callback;

import java.util.WeakHashMap;

import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.core.Instance;
import sh.emberj.annotate.mixin.MethodProber;
import sh.emberj.annotate.networking.callback.NetworkCallbacks.AmbiguousCallback;
import sh.emberj.annotate.registry.Register;
import sh.emberj.annotate.registry.RegistryManager;
import sh.emberj.annotate.registry.SimpleRegistry;

@Register(registry = RegistryManager.ID, stage = AnnotateLoadStage.PRELAUNCH)
public class NetCallbackRegistry extends SimpleRegistry<NetCallbackInfo> {

    public static String ID = "annotate:net_callback";
    @Instance
    public static final NetCallbackRegistry INSTANCE = new NetCallbackRegistry();

    private final WeakHashMap<Object, NetCallbackInfo> _LAMBDA_CACHE;

    private NetCallbackRegistry() {
        super(new Identifier(ID), NetCallbackInfo.class);
        _LAMBDA_CACHE = new WeakHashMap<>();
    }

    @Override
    public void register(Identifier key, NetCallbackInfo value) throws AnnotateException {
        super.register(key, value);
        MethodProber.setupProbe(value.getMethodMeta(), key.toString());
    }

    public NetCallbackInfo findCallbackInfo(AmbiguousCallback<?, ?> callback) throws AnnotateException {
        NetCallbackInfo info = _LAMBDA_CACHE.get(callback);
        if (info != null)
            return info;
        String probeResults = MethodProber.probe(() -> callback.invoke(null, null), true);
        if (probeResults == null)
            throw new AnnotateException("Invalid callback. Couldn't probe callback info.");
        info = get(new Identifier(probeResults));
        if (info == null)
            throw new AnnotateException("Invalid callback. Callback probing returned unknown id " + probeResults + ".");
        _LAMBDA_CACHE.put(callback, info);
        return info;
    }
}
