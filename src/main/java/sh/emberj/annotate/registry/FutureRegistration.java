package sh.emberj.annotate.registry;

import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateIdentifier;
import sh.emberj.annotate.core.AnnotatedClass;
import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.core.ILoadListener;

public class FutureRegistration implements ILoadListener {

    private final AnnotateLoadStage _STAGE;
    private final int _PRIORITY;
    private final AnnotatedClass _CLASS;
    private final Identifier _ITEM_ID, _REGISTRY_ID;

    public FutureRegistration(AnnotatedClass class_, String registry, String path, String namespace,
            AnnotateLoadStage stage, int priority) throws AnnotateException {
        _STAGE = stage;
        _PRIORITY = priority;
        _CLASS = class_;

        Identifier id = null;
        if ((path == null || path.isBlank()) && (namespace == null || namespace.isBlank())) {
            IIdentifiable instance = _CLASS.tryCastInstance(IIdentifiable.class);
            if (instance != null)
                id = instance.getIdentifier();
        }

        if (id == null)
            id = AnnotateIdentifier.createIdentifier(namespace, path, class_);

        _REGISTRY_ID = AnnotateIdentifier.createIdentifier(registry, class_);
        _ITEM_ID = id;
    }

    @Override
    public int getPriority() {
        return _PRIORITY;
    }

    @Override
    public AnnotateLoadStage getLoadStage() {
        return _STAGE;
    }

    @Override
    public void onLoad() throws AnnotateException {
        RegistryManager.register(_REGISTRY_ID, _ITEM_ID, _CLASS.getInstance());
    }
}
