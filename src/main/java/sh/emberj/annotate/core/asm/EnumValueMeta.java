package sh.emberj.annotate.core.asm;

import org.objectweb.asm.Type;

import sh.emberj.annotate.core.AnnotateException;

public class EnumValueMeta {

    private final String _DESCRIPTOR, _VALUE;

    public EnumValueMeta(String descriptor, String value) {
        _DESCRIPTOR = descriptor;
        _VALUE = value;
    }

    public <T extends Enum<T>> T getValue(Class<T> annotationClass) throws AnnotateException {
        Type annotationType = Type.getType(annotationClass);
        if (!annotationType.getDescriptor().equals(_DESCRIPTOR))
            throw new AnnotateException("Annotation class was not the expected class of " + _DESCRIPTOR + ".");
        return Enum.valueOf(annotationClass, _VALUE);
    }

    public String getValue() {
        return _VALUE;
    }

    public String getDescriptor() {
        return _DESCRIPTOR;
    }
}
