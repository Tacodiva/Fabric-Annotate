package sh.emberj.annotate.core.asm;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.Utils;

public class AnnotatedTypeMeta extends AnnotatedMeta {

    private static Map<String, AnnotatedTypeMeta> _CAHCE = new HashMap<>();

    public static int getCacheSize() {
        return _CAHCE.size();
    }

    public static AnnotatedTypeMeta readMetadata(Class<?> clazz) throws AnnotateException {
        return readMetadata(Type.getType(clazz));
    }

    public static AnnotatedTypeMeta readMetadata(Type type) throws AnnotateException {
        return readMetadata(type, false);
    }

    public static AnnotatedTypeMeta readMetadata(Type type, boolean allowNotFound) throws AnnotateException {
        if (_CAHCE.containsKey(type.getClassName())) {
            AnnotatedTypeMeta meta = _CAHCE.get(type.getClassName());
            if (meta == null && !allowNotFound)
                throw new AnnotateException("Class '" + type.getClassName() + "' not found.");
            return meta;
        }
        AnnotatedTypeMeta meta;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream classStream = classLoader.getResourceAsStream(type.getInternalName() + ".class");
            if (classStream == null) {
                if (allowNotFound) {
                    Annotate.LOG.warn("Could not find class '" + type.getInternalName() + "'.");
                    _CAHCE.put(type.getClassName(), null);
                    return null;
                }
                throw new AnnotateException("Class '" + type.getClassName() + "' not found.");
            }
            byte[] classFile = classStream.readAllBytes();
            ClassReader cr = new ClassReader(classFile);
            AnnotatedTypeVisitor visitor = new AnnotatedTypeVisitor(type);
            cr.accept(visitor, 0);
            meta = visitor.getTarget();
        } catch (IOException e) {
            throw new AnnotateException("Error while reading type metadata of '" + type.getClassName() + "'.", e);
        }
        _CAHCE.put(type.getClassName(), meta);
        return meta;
    }

    private final List<AnnotatedMethodMeta> _METHODS;
    private final Type _TYPE;

    private final Type _SUPERTYPE;
    private final Type[] _INTERFACES;

    private Class<?> _class;

    AnnotatedTypeMeta(Type type, String superclass, String[] interfaces) {
        _METHODS = new ArrayList<>();
        _TYPE = type;
        _SUPERTYPE = Type.getType("L" + superclass + ";");
        _INTERFACES = new Type[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            _INTERFACES[i] = Type.getType("L" + interfaces[i] + ";");
        }
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

    public AnnotatedMethodMeta getMethod(String name, String descriptor) {
        return _METHODS.stream()
                .filter(method -> method.getName().equals(name) && method.getDescriptor().equals(descriptor)).findAny()
                .orElse(null);
    }

    public Type getSupertype() {
        return _SUPERTYPE;
    }

    public Type[] getInterfaces() {
        return _INTERFACES;
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

    @Override
    public String toString() {
        return _TYPE.toString();
    }
}
