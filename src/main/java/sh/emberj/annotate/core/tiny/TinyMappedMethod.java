package sh.emberj.annotate.core.tiny;

import net.fabricmc.mapping.tree.MethodDef;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.asm.MethodMetadata;

public class TinyMappedMethod {
    private final MethodDef _METHOD_DEF;
    private final TinyMappedClass _CLASS;
    private final String _NAME, _DESCRIPTOR;
    private final TinyMapper _MAPPER;

    TinyMappedMethod(TinyMapper mapper, MethodDef methodDef, TinyMappedClass clazz) {
        _METHOD_DEF = methodDef;
        _CLASS = clazz;
        _NAME = _METHOD_DEF.getName(getNamespace().getId());
        _DESCRIPTOR = _METHOD_DEF.getDescriptor(getNamespace().getId());
        _MAPPER = mapper;
    }

    public MethodDef getMethodDef() {
        return _METHOD_DEF;
    }

    public TinyMappedMethod mapTo(TinyNamespace namespace) {
        if (getNamespace() == namespace)
            return this;
        return _CLASS.mapTo(namespace).getMethod(mapNameTo(namespace), mapDescriptorTo(namespace));
    }

    public String mapNameTo(TinyNamespace namespace) {
        if (getNamespace() == namespace)
            return _NAME;
        return _METHOD_DEF.getName(namespace.getId());
    }

    public String getName() {
        return _NAME;
    }

    public String mapDescriptorTo(TinyNamespace namespace) {
        if (getNamespace() == namespace)
            return _DESCRIPTOR;
        return _METHOD_DEF.getDescriptor(namespace.getId());
    }

    public String getDescriptor() {
        return _DESCRIPTOR;
    }

    public TinyNamespace getNamespace() {
        return _CLASS.getNamespace();
    }

    public MethodMetadata getMethodMetadata() throws AnnotateException {
        TinyMappedMethod classpathName = mapTo(_MAPPER.getClasspathNamespace());
        return classpathName._CLASS.getClassMetadata().getMethod(classpathName.getName(),
                classpathName.getDescriptor());
    }
}
