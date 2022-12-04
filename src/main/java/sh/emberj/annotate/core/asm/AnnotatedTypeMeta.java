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

public class AnnotatedTypeMeta extends AnnotatedMeta {

    private static Map<String, AnnotatedTypeMeta> _CAHCE = new HashMap<>();

    public static AnnotatedTypeMeta readMetadata(Class<?> clazz) throws AnnotateException {
        return readMetadata(clazz.getCanonicalName());
    }

    public static AnnotatedTypeMeta readMetadata(String clazz) throws AnnotateException {
        AnnotatedTypeMeta meta = _CAHCE.get(clazz);
        if (meta != null) return meta;
        meta = new AnnotatedTypeMeta();
        try {
            ClassReader cr = new ClassReader(clazz);
            cr.accept(new AnnotatedTypeVisitor(meta), 0);
        } catch (IOException e) {
            throw new AnnotateException("Error while reading type metadata of '" + clazz + "'.", e);
        }
        _CAHCE.put(clazz, meta);
        return meta;
    }

    private List<AnnotatedMethodMeta> _METHODS;

    AnnotatedTypeMeta() {
        _METHODS = new ArrayList<>();
    }

    void addMethod(AnnotatedMethodMeta method) {
        _METHODS.add(method);
    }

    public AnnotatedMethodMeta[] getMethodsByName(String name) {
        return _METHODS.stream().filter(method -> method.getName().equals(name)).toArray(AnnotatedMethodMeta[]::new);
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
}
