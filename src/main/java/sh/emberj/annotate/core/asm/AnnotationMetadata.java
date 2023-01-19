package sh.emberj.annotate.core.asm;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import sh.emberj.annotate.core.AnnotateException;

public class AnnotationMetadata {

    private Type _TYPE;
    protected Map<String, Object> _PARAMETERS;

    public AnnotationMetadata(Class<? extends Annotation> type) {
        this(Type.getType(type));
    }

    public AnnotationMetadata(Type type) {
        _TYPE = type;
        _PARAMETERS = new HashMap<>();
    }

    public Type getType() {
        return _TYPE;
    }

    void addParam(String name, Object value) {
        _PARAMETERS.put(name, value);
    }

    public Object getParam(String name) {
        return _PARAMETERS.get(name);
    }

    public Type getClassParam(String name) {
        return (Type) _PARAMETERS.get(name);
    }

    public String getStringParam(String name) {
        return (String) _PARAMETERS.get(name);
    }

    public Integer getIntParam(String name) {
        return (Integer) _PARAMETERS.get(name);
    }

    public int getIntParam(String name, int defaultValue) {
        final Integer value = getIntParam(name);
        if (value == null)
            return defaultValue;
        return value;
    }

    public AnnotationEnumMetadata getEnumMetadataParam(String name) {
        return (AnnotationEnumMetadata) _PARAMETERS.get(name);
    }

    public String getEnumParamValue(String name) {
        AnnotationEnumMetadata value = getEnumMetadataParam(name);
        if (value == null)
            return null;
        return value.getValue();
    }

    public <T extends Enum<T>> T getEnumParam(String name, Class<T> enumClass) throws AnnotateException {
        return getEnumParam(name, enumClass, null);
    }

    public <T extends Enum<T>> T getEnumParam(String name, Class<T> enumClass, T defaultValue)
            throws AnnotateException {
        AnnotationEnumMetadata value = getEnumMetadataParam(name);
        if (value == null)
            return defaultValue;
        return value.getValue(enumClass);
    }

    public AnnotationArrayMetadata getArrayParam(String name) {
        return (AnnotationArrayMetadata) _PARAMETERS.get(name);
    }

    public AnnotationMetadata getAnnotationParam(String name) {
        return (AnnotationMetadata) _PARAMETERS.get(name);
    }

    public Boolean getBooleanParam(String name) {
        return (Boolean) _PARAMETERS.get(name);
    }

    public boolean getBooleanParam(String name, boolean defaultValue) {
        Boolean value = getBooleanParam(name);
        if (value == null)
            return defaultValue;
        return value;
    }

    public void write(AnnotationVisitor aw) {
        for (Entry<String, Object> param : _PARAMETERS.entrySet())
            write(param.getKey(), param.getValue(), aw);
        aw.visitEnd();
    }

    static void write(String key, Object value, AnnotationVisitor aw) {
        if (value instanceof AnnotationMetadata annotation)
            annotation.write(aw.visitAnnotation(key, annotation.getType().getDescriptor()));
        else if (value instanceof AnnotationEnumMetadata enum_)
            aw.visitEnum(key, enum_.getDescriptor(), enum_.getValue());
        else if (value instanceof AnnotationArrayMetadata array) {
            AnnotationVisitor arrayVisitor = aw.visitArray(key);
            for (int i = 0; i < array.size(); i++)
                write(null, array.get(i), arrayVisitor);
            arrayVisitor.visitEnd();
        } else {
            aw.visit(key, value);
        }
    }
}
