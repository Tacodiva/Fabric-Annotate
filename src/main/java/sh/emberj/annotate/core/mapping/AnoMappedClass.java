package sh.emberj.annotate.core.mapping;

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
import sh.emberj.annotate.core.asm.AnnotatedTypeMeta;

public class AnoMappedClass {
    private final ClassDef _CLASS_DEF;
    private final AnoNamespace _NAMESPACE;
    private final String _NAME;
    private final Map<String, Map<String, AnoMappedMethod>> _METHODS_MAP;
    private final List<AnoMappedMethod> _METHODS;
    private final Map<String, AnoMappedField> _FIELDS;
    private final AnoMapper _MAPPER;
    private final AnnotatedTypeMeta _TYPE_META;

    AnoMappedClass(AnoMapper mapper, AnnotatedTypeMeta meta, ClassDef classDef, AnoNamespace namespace) throws AnnotateException {
        _CLASS_DEF = classDef;
        _NAMESPACE = namespace;
        _MAPPER = mapper;
        _NAME = classDef.getName(namespace.getId());
        _TYPE_META = meta;

        _METHODS = new ArrayList<>();
        _METHODS_MAP = new HashMap<>();
        for (MethodDef methodDef : _CLASS_DEF.getMethods()) {
            addMethod(new AnoMappedMethod(mapper, methodDef, this));
        }

        _FIELDS = new HashMap<>();
        for (FieldDef fieldDef : _CLASS_DEF.getFields()) {
            addField(new AnoMappedField(fieldDef, this));
        }
    }

    void resolveInheritance() {
        addInheritedProperties(_TYPE_META.getSupertype());
        for (Type interf : _TYPE_META.getInterfaces())
            addInheritedProperties(interf);
    }

    private void addInheritedProperties(Type type) {
        AnoMappedClass typeClass = _MAPPER.getClasspathNamespace().getClass(type);
        if (typeClass != null) {
            for (AnoMappedMethod method : typeClass.getMethods())
                addMethod(method.mapTo(_NAMESPACE));
            for (AnoMappedField field : typeClass.getFields())
                addField(field.mapTo(_NAMESPACE));
        }
    }

    private void addMethod(AnoMappedMethod method) {
        Map<String, AnoMappedMethod> namedMethods = _METHODS_MAP.computeIfAbsent(method.getName(),
                a -> new HashMap<>());
        if (!namedMethods.containsKey(method.getDescriptor())) {
            namedMethods.put(method.getDescriptor(), method);
            _METHODS.add(method);
        }
    }

    private void addField(AnoMappedField field) {
        if (!_FIELDS.containsKey(field.getName()))
            _FIELDS.put(field.getName(), field);
    }

    public Collection<AnoMappedMethod> getMethods() {
        return _METHODS;
    }

    public Collection<AnoMappedMethod> getMethods(String name) {
        Map<String, AnoMappedMethod> methodsWithName = _METHODS_MAP.get(name);
        if (methodsWithName == null)
            return Collections.emptySet();
        return methodsWithName.values();
    }

    public AnoMappedMethod getMethod(String name, String desciptor) {
        Map<String, AnoMappedMethod> methodsWithName = _METHODS_MAP.get(name);
        if (methodsWithName == null)
            return null;
        return methodsWithName.get(desciptor);
    }

    public Collection<AnoMappedField> getFields() {
        return _FIELDS.values();
    }

    public AnoMappedField getField(String name) {
        return _FIELDS.get(name);
    }

    public AnoMappedClass mapTo(AnoNamespace namespace) {
        if (_NAMESPACE == namespace)
            return this;
        return namespace.getClass(mapNameTo(namespace));
    }

    public String getName() {
        return _NAME;
    }

    public String mapNameTo(AnoNamespace namespace) {
        if (_NAMESPACE == namespace)
            return _NAME;
        return _CLASS_DEF.getName(namespace.getId());
    }

    public AnoNamespace getNamespace() {
        return _NAMESPACE;
    }

    public ClassDef getClassDefinition() {
        return _CLASS_DEF;
    }

    public AnnotatedTypeMeta getTypeMeta() {
        return _TYPE_META;
    }
}
