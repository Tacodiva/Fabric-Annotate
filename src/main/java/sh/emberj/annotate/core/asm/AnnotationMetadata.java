package sh.emberj.annotate.core.asm;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;

import sh.emberj.annotate.core.AnnotateException;

public class AnnotationMetadata {

    private Type _TYPE;
    private Map<String, Object> _PARAMETERS;

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
        if (value == null) return defaultValue;
        return value;
    }

}
