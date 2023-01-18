package sh.emberj.annotate.core.asm;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;

import sh.emberj.annotate.core.Utils;

public class ClassMetadata extends AnnotationContainer {

    private final List<MethodMetadata> _METHODS;
    private final Type _TYPE;

    private final Type _SUPERTYPE;
    private final Type[] _INTERFACES;

    private Class<?> _class;

    ClassMetadata(Type type, String superclass, String[] interfaces) {
        _METHODS = new ArrayList<>();
        _TYPE = type;
        _SUPERTYPE = Type.getType("L" + superclass + ";");
        _INTERFACES = new Type[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            _INTERFACES[i] = Type.getType("L" + interfaces[i] + ";");
        }
    }

    void addMethod(MethodMetadata method) {
        _METHODS.add(method);
    }

    public MethodMetadata[] getMethodsByName(String name) {
        return _METHODS.stream().filter(method -> method.getName().equals(name)).toArray(MethodMetadata[]::new);
    }

    public MethodMetadata[] getMethodsByAnnotation(Class<?> annotation) {
        return getMethodsByAnnotation(Type.getType(annotation));
    }

    public MethodMetadata[] getMethodsByAnnotation(Type annotation) {
        return _METHODS.stream().filter(method -> method.hasAnnotation(annotation)).toArray(MethodMetadata[]::new);
    }

    public MethodMetadata getMethod(Method method) {
        String methodDesc = Type.getMethodDescriptor(method);
        return _METHODS.stream()
                .filter(meta -> meta.getName().equals(method.getName()) && meta.getDescriptor().equals(methodDesc))
                .findFirst().orElse(null);
    }

    public MethodMetadata getMethod(String name, Type[] arguments) {
        return _METHODS.stream().filter(target -> {
            if (!target.getName().equals(name))
                return false;
            Type[] targetArgs = target.getArgTypes();
            if (targetArgs.length != arguments.length)
                return false;
            for (int i = 0; i < targetArgs.length; i++) {
                if (!arguments[i].equals(targetArgs[i]))
                    return false;
            }
            return true;
        }).findAny().orElse(null);
    }

    public MethodMetadata getMethod(String name, String descriptor) {
        return _METHODS.stream()
                .filter(method -> method.getName().equals(name) && method.getDescriptor().equals(descriptor)).findAny()
                .orElse(null);
    }

    public String getSimpleName() {
        String[] parts = _TYPE.getInternalName().split("/");
        return parts[parts.length - 1];
    }

    public Type getSupertype() {
        return _SUPERTYPE;
    }

    public Type[] getInterfaces() {
        return _INTERFACES;
    }

    public Iterable<MethodMetadata> getMethods() {
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
