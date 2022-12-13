package sh.emberj.annotate.core.asm;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;

import sh.emberj.annotate.core.AnnotateException;

public class AnnotationMeta {

    private Type _TYPE;
    private Map<String, Object> _PARAMETERS;
    private Map<String, EnumValueMeta> _ENUM_PARAMS;

    public AnnotationMeta(Type type) {
        _TYPE = type;
        _PARAMETERS = new HashMap<>();
        _ENUM_PARAMS = new HashMap<>();
    }

    public Type getType() {
        return _TYPE;
    }

    void addParam(String name, Object value) {
        _PARAMETERS.put(name, value);
    }

    void addEnumParam(String name, EnumValueMeta value) {
        _ENUM_PARAMS.put(name, value);
    }

    public Object getParam(String name) {
        return _PARAMETERS.get(name);
    }

    public Type getTypeParam(String name) {
        return (Type) _PARAMETERS.get(name);
    }

    public String getStringParam(String name) {
        return (String) _PARAMETERS.get(name);
    }

    public String getEnumParamValue(String name) {
        EnumValueMeta value = _ENUM_PARAMS.get(name);
        if (value == null) return null;
        return value.getValue();
    }

    public <T extends Enum<T>> T getEnumParam(String name, Class<T> enumClass) throws AnnotateException {   
        EnumValueMeta value = _ENUM_PARAMS.get(name);
        if (value == null) return null;
        return value.getValue(enumClass);
    }
}
