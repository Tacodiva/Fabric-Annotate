package sh.emberj.annotate.core.asm;

import org.objectweb.asm.Type;

import sh.emberj.annotate.core.AnnotateException;

public class AnnotationEnumMetadata {

    private final String _DESCRIPTOR, _VALUE;

    public AnnotationEnumMetadata(String descriptor, String value) {
        _DESCRIPTOR = descriptor;
        _VALUE = value;
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getValue(Class<T> annotationClass) throws AnnotateException {
        Type annotationType = Type.getType(annotationClass);
        if (!annotationType.getDescriptor().equals(_DESCRIPTOR))
            throw new AnnotateException("Annotation class was not the expected class of " + _DESCRIPTOR + ".");
        try {
            // We have to use getDeclaredField instead of Enum.valueOf so this works
            //  when the enum has been obfuscated.
            return (T) annotationClass.getDeclaredField(_VALUE).get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new AnnotateException("Unexpected exception while getting annotation enum value.", e);
        }
    }

    public String getValue() {
        return _VALUE;
    }

    public String getDescriptor() {
        return _DESCRIPTOR;
    }
}
