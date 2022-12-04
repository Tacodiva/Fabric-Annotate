package sh.emberj.annotate.core.asm;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;

public class AnnotationMeta {

    private Type _TYPE;
    private Map<String, Object> _PARAMETERS;

    public AnnotationMeta(Type type) {
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

    public Type getTypeParam(String name) {
        return (Type) _PARAMETERS.get(name);
    }

    public String getStringParam(String name) {
        return (String) _PARAMETERS.get(name);
    }

    // public Type getTypeParam(String name) {
    //     Object obj = _PARAMETERS.get(name);
    //     // return obj;
    // }
}
