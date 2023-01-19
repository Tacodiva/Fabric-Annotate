package sh.emberj.annotate.core.asm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;

import sh.emberj.annotate.core.AnnotateException;

public class AnnotationArrayMetadata {

    protected final List<Object> _CONTENTS;

    public AnnotationArrayMetadata() {
        _CONTENTS = new ArrayList<>();
    }

    void add(Object obj) {
        _CONTENTS.add(obj);
    }

    public int size() {
        return _CONTENTS.size();
    }

    public Object get(int name) {
        return _CONTENTS.get(name);
    }

    public Type getClass(int name) {
        return (Type) _CONTENTS.get(name);
    }

    public String getString(int name) {
        return (String) _CONTENTS.get(name);
    }

    public Integer getInt(int name) {
        return (Integer) _CONTENTS.get(name);
    }

    public int getInt(int name, int defaultValue) {
        final Integer value = getInt(name);
        if (value == null)
            return defaultValue;
        return value;
    }

    public AnnotationEnumMetadata getEnumMetadata(int name) {
        return (AnnotationEnumMetadata) _CONTENTS.get(name);
    }

    public String getEnumParamValue(int name) {
        AnnotationEnumMetadata value = getEnumMetadata(name);
        if (value == null)
            return null;
        return value.getValue();
    }

    public <T extends Enum<T>> T getEnum(int name, Class<T> enumClass) throws AnnotateException {
        return getEnum(name, enumClass, null);
    }

    public <T extends Enum<T>> T getEnum(int name, Class<T> enumClass, T defaultValue)
            throws AnnotateException {
        AnnotationEnumMetadata value = getEnumMetadata(name);
        if (value == null)
            return defaultValue;
        return value.getValue(enumClass);
    }

    public AnnotationArrayMetadata getArray(int name) {
        return (AnnotationArrayMetadata) _CONTENTS.get(name);
    }

    public AnnotationMetadata getAnnotation(int name) {
        return (AnnotationMetadata) _CONTENTS.get(name);
    }
}
