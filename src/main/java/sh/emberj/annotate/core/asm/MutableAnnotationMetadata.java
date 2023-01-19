package sh.emberj.annotate.core.asm;

import java.lang.annotation.Annotation;

import org.objectweb.asm.Type;

public class MutableAnnotationMetadata extends AnnotationMetadata {

    public MutableAnnotationMetadata(Class<? extends Annotation> type) {
        super(type);
    }

    public MutableAnnotationMetadata(Type type) {
        super(type);
    }

    void setParam(String name, Object value) {
        _PARAMETERS.put(name, value);
    }

    public void setClassParam(String name, Type value) {
        _PARAMETERS.put(name, value);
    }

    public void setStringParam(String name, String value) {
        _PARAMETERS.put(name, value);
    }

    public void setIntParam(String name, int value) {
        _PARAMETERS.put(name, value);
    }

    public void setEnumParam(String name, AnnotationEnumMetadata value) {
        _PARAMETERS.put(name, value);
    }

    public <T extends Enum<T>> void setEnumParam(String name, T value) {
        _PARAMETERS.put(name, new AnnotationEnumMetadata(Type.getType(value.getClass()).getDescriptor(), value.name()));
    }

    public void setArrayParam(String name, AnnotationArrayMetadata value) {
        _PARAMETERS.put(name, value);
    }

    public void setAnnotationParam(String name, AnnotationMetadata value) {
        _PARAMETERS.put(name, value);
    }

    public void setBooleanParam(String name, boolean value) {
        _PARAMETERS.put(name, value);
    }
}
