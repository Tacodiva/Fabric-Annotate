package sh.emberj.annotate.core.mapping;

import net.fabricmc.mapping.tree.MethodDef;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.asm.AnnotatedMethodMeta;

public class AnoMappedMethod {
    private final MethodDef _METHOD_DEF;
    private final AnoMappedClass _CLASS;
    private final String _NAME, _DESCRIPTOR;
    private final AnoMapper _MAPPER;

    AnoMappedMethod(AnoMapper mapper, MethodDef methodDef, AnoMappedClass clazz) {
        _METHOD_DEF = methodDef;
        _CLASS = clazz;
        _NAME = _METHOD_DEF.getName(getNamespace().getId());
        _DESCRIPTOR = _METHOD_DEF.getDescriptor(getNamespace().getId());
        _MAPPER = mapper;
    }

    public AnoMappedMethod mapTo(AnoNamespace namespace) {
        if (getNamespace() == namespace) return this;
        return _CLASS.mapTo(namespace).getMethod(mapNameTo(namespace), mapDescriptorTo(namespace));
    }

    public String mapNameTo(AnoNamespace namespace) {
        if (getNamespace() == namespace) return _NAME;
        return _METHOD_DEF.getName(namespace.getId());
    }

    public String getName() {
        return _NAME;
    }

    public String mapDescriptorTo(AnoNamespace namespace) {
        if (getNamespace() == namespace) return _DESCRIPTOR;
        return _METHOD_DEF.getDescriptor(namespace.getId());
    }

    public String getDescriptor() {
        return _DESCRIPTOR;
    }

    public AnoNamespace getNamespace() {
        return _CLASS.getNamespace();
    }

    public AnnotatedMethodMeta getMethodMeta() throws AnnotateException {
        AnoMappedMethod classpathName = mapTo(_MAPPER.getClasspathNamespace());
        return classpathName._CLASS.getTypeMeta().getMethod(classpathName.getName(), classpathName.getDescriptor());
    }
}
