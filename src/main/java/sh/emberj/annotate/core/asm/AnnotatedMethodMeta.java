package sh.emberj.annotate.core.asm;

import java.lang.reflect.Method;

import org.objectweb.asm.Type;

import sh.emberj.annotate.core.Utils;

public class AnnotatedMethodMeta extends AnnotatedMeta {
    private final AnnotatedTypeMeta _TYPE;
    private final String _NAME, _DESCRIPTOR;
    private final String[] _EXCEPTIONS;
    private final int _MODIFIERS;

    private Method _method;
    private Type _returnType;
    private Type[] _args;

    public AnnotatedMethodMeta(AnnotatedTypeMeta type, String name, String descriptor, int modifiers,
            String[] exceptions) {
        _TYPE = type;
        _NAME = name;
        _DESCRIPTOR = descriptor;
        _EXCEPTIONS = exceptions;
        _MODIFIERS = modifiers;
    }

    public Method getMethod() {
        if (_method != null)
            return _method;
        Type[] args = getArgTypes();
        Class<?>[] argClasses = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++)
            argClasses[i] = Utils.loadClass(args[i]);
        try {
            _method = _TYPE.getAsClass().getMethod(_NAME, argClasses);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        return _method;
    }

    public AnnotatedTypeMeta getDeclaringType() {
        return _TYPE;
    }

    public String getName() {
        return _NAME;
    }

    public String getDescriptor() {
        return _DESCRIPTOR;
    }

    public Type getReturnType() {
        if (_returnType != null)
            return _returnType;
        return _returnType = Type.getReturnType(getDescriptor());
    }

    public int getArgNum() {
        return getArgTypes().length;
    }

    public Type[] getArgTypes() {
        if (_args != null)
            return _args;
        return _args = Type.getArgumentTypes(getDescriptor());
    }

    public String[] getExceptions() {
        return _EXCEPTIONS;
    }

    public int getModifiers() {
        return _MODIFIERS;
    }

    public boolean hasModifier(int modifier) {
        return (_MODIFIERS & modifier) != 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getReturnType().getClassName());
        sb.append(" ");
        sb.append(getName());
        sb.append("(");
        Type[] argTypes = getArgTypes();
        if (argTypes.length != 0) {
            sb.append(argTypes[0].getClassName());
            for (int i = 1; i < argTypes.length; i++) {
                sb.append(", ");
                sb.append(argTypes[i].getClassName());
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
