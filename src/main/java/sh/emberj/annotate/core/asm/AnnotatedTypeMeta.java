package sh.emberj.annotate.core.asm;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.Utils;

public class AnnotatedTypeMeta extends AnnotatedMeta {

    private static Map<String, AnnotatedTypeMeta> _CAHCE = new HashMap<>();

    public static AnnotatedTypeMeta readMetadata(Class<?> clazz) throws AnnotateException {
        return readMetadata(Type.getType(clazz));
    }

    public static AnnotatedTypeMeta readMetadata(Type type) throws AnnotateException {
        AnnotatedTypeMeta meta = _CAHCE.get(type.getClassName());
        if (meta != null)
            return meta;
        meta = new AnnotatedTypeMeta(type);
        try {
            ClassReader cr = new ClassReader(type.getClassName());
            cr.accept(new AnnotatedTypeVisitor(meta), 0);
        } catch (IOException e) {
            throw new AnnotateException("Error while reading type metadata of '" + type.getClassName() + "'.", e);
        }
        _CAHCE.put(type.getClassName(), meta);
        return meta;
    }

    private final List<AnnotatedMethodMeta> _METHODS;
    private final Type _TYPE;

    private Class<?> _class;

    AnnotatedTypeMeta(Type type) {
        _METHODS = new ArrayList<>();
        _TYPE = type;
    }

    void addMethod(AnnotatedMethodMeta method) {
        _METHODS.add(method);
    }

    public AnnotatedMethodMeta[] getMethodsByName(String name) {
        return _METHODS.stream().filter(method -> method.getName().equals(name)).toArray(AnnotatedMethodMeta[]::new);
    }

    public AnnotatedMethodMeta[] getMethodsByAnnotation(Class<?> annotation) {
        return getMethodsByAnnotation(Type.getType(annotation));
    }

    public AnnotatedMethodMeta[] getMethodsByAnnotation(Type annotation) {
        return _METHODS.stream().filter(method -> method.hasAnnotation(annotation)).toArray(AnnotatedMethodMeta[]::new);
    }

    public AnnotatedMethodMeta getMethod(Method method) {
        String methodDesc = Type.getMethodDescriptor(method);
        return _METHODS.stream()
                .filter(meta -> meta.getName().equals(method.getName()) && meta.getDescriptor().equals(methodDesc))
                .findFirst().orElse(null);
    }

    public Iterable<AnnotatedMethodMeta> getMethods() {
        return _METHODS;
    }

    public Type getType() {
        return _TYPE;
    }

    public Class<?> getAsClass() {
        if (_class != null)
            return _class;
        return _class = Utils.loadClass(_TYPE);
    }
}
