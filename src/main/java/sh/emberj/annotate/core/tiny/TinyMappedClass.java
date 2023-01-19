package sh.emberj.annotate.core.tiny;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.FieldDef;
import net.fabricmc.mapping.tree.MethodDef;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.asm.ClassMetadata;
import sh.emberj.annotate.core.asm.MethodMetadata;

public class TinyMappedClass {
    private final ClassDef _CLASS_DEF;
    private final TinyNamespace _NAMESPACE;
    private final String _NAME;
    private final Map<String, Map<String, TinyMappedMethod>> _METHODS_MAP;
    private final List<TinyMappedMethod> _METHODS;
    private final Map<String, TinyMappedField> _FIELDS;
    private final TinyMapper _MAPPER;
    private final ClassMetadata _TYPE_META;

    TinyMappedClass(TinyMapper mapper, ClassMetadata meta, ClassDef classDef, TinyNamespace namespace)
            throws AnnotateException {
        _CLASS_DEF = classDef;
        _NAMESPACE = namespace;
        _MAPPER = mapper;
        _NAME = classDef.getName(namespace.getId());
        _TYPE_META = meta;

        _METHODS = new ArrayList<>();
        _METHODS_MAP = new HashMap<>();
        for (MethodDef methodDef : _CLASS_DEF.getMethods()) {
            addMethod(new TinyMappedMethod(mapper, methodDef, this));
        }

        _FIELDS = new HashMap<>();
        for (FieldDef fieldDef : _CLASS_DEF.getFields()) {
            addField(new TinyMappedField(fieldDef, this));
        }
    }

    void resolveInheritance() {
        addInheritedProperties(_TYPE_META.getSupertype());
        for (Type interf : _TYPE_META.getInterfaces())
            addInheritedProperties(interf);
    }

    private void addInheritedProperties(Type type) {
        TinyMappedClass typeClass = _MAPPER.getClasspathNamespace().getClass(type);
        if (typeClass != null) {
            for (TinyMappedMethod method : typeClass.getMethods()) {
                MethodMetadata ourMethod = _TYPE_META.getMethod(method.getName(), method.getDescriptor());
                if (ourMethod != null)
                    addMethod(new TinyMappedMethod(_MAPPER, method.getMethodDef(), this));
            }
            for (TinyMappedField field : typeClass.getFields())
                addField(field.mapTo(_NAMESPACE));
        }
    }

    private void addMethod(TinyMappedMethod method) {
        Map<String, TinyMappedMethod> namedMethods = _METHODS_MAP.computeIfAbsent(method.getName(),
                a -> new HashMap<>());
        if (!namedMethods.containsKey(method.getDescriptor())) {
            namedMethods.put(method.getDescriptor(), method);
            _METHODS.add(method);
        }
    }

    private void addField(TinyMappedField field) {
        if (!_FIELDS.containsKey(field.getName()))
            _FIELDS.put(field.getName(), field);
    }

    public Collection<TinyMappedMethod> getMethods() {
        return _METHODS;
    }

    public Collection<TinyMappedMethod> getMethods(String name) {
        Map<String, TinyMappedMethod> methodsWithName = _METHODS_MAP.get(name);
        if (methodsWithName == null)
            return Collections.emptySet();
        return methodsWithName.values();
    }

    public TinyMappedMethod getMethod(String name, String desciptor) {
        Map<String, TinyMappedMethod> methodsWithName = _METHODS_MAP.get(name);
        if (methodsWithName == null)
            return null;
        return methodsWithName.get(desciptor);
    }

    public Collection<TinyMappedField> getFields() {
        return _FIELDS.values();
    }

    public TinyMappedField getField(String name) {
        return _FIELDS.get(name);
    }

    public TinyMappedClass mapTo(TinyNamespace namespace) {
        if (_NAMESPACE == namespace)
            return this;
        return namespace.getClass(mapNameTo(namespace));
    }

    public String getName() {
        return _NAME;
    }

    public String mapNameTo(TinyNamespace namespace) {
        if (_NAMESPACE == namespace)
            return _NAME;
        return _CLASS_DEF.getName(namespace.getId());
    }

    public TinyNamespace getNamespace() {
        return _NAMESPACE;
    }

    public ClassDef getClassDefinition() {
        return _CLASS_DEF;
    }

    public ClassMetadata getClassMetadata() {
        return _TYPE_META;
    }
}
