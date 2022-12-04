package sh.emberj.annotate.core.asm;

import org.objectweb.asm.Type;

public class AnnotatedMethodMeta extends AnnotatedMeta {
    private final String _NAME, _DESCRIPTOR;
    private final String[] _EXCEPTIONS;
    private final int _MODIFIERS;
    
    public AnnotatedMethodMeta(String name, String descriptor, int modifiers, String[] exceptions) {
        _NAME = name;
        _DESCRIPTOR = descriptor;
        _EXCEPTIONS = exceptions;
        _MODIFIERS = modifiers;
    }

    public String getName() {
        return _NAME;
    }

    public String getDescriptor() {
        return _DESCRIPTOR;
    }

    public Type getReturnType() {
        return Type.getReturnType(getDescriptor());
    }

    public Type[] getArgTypes() {
        return Type.getArgumentTypes(getDescriptor());
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
}
