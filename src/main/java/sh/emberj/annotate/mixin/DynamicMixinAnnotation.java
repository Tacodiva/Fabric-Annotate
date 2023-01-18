package sh.emberj.annotate.mixin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

public class DynamicMixinAnnotation {
    private final Type _TYPE;
    private final boolean _VISIBLE;
    private Map<String, Object> _NORMAL_PARAMS;
    private Map<String, Object[]> _ARR_PARAMS;
    private Map<String, DynamicMixinAnnotation> _ANNOTATION_PARAMS;
    private Map<String, DynamicMixinAnnotation[]> _ANNOTATION_ARR_PARAMS;

    public DynamicMixinAnnotation(Class<?> annotationType, boolean visible) {
        this(Type.getType(annotationType), visible);
    }

    public DynamicMixinAnnotation(Type annotationType, boolean visible) {
        _TYPE = annotationType;
        _NORMAL_PARAMS = new HashMap<>();
        _ARR_PARAMS = new HashMap<>();
        _ANNOTATION_PARAMS = new HashMap<>();
        _ANNOTATION_ARR_PARAMS = new HashMap<>();
        _VISIBLE = visible;
    }

    public DynamicMixinAnnotation setParam(String name, Object value) {
        _NORMAL_PARAMS.put(name, value);
        return this;
    }

    public DynamicMixinAnnotation setArrayParam(String name, Object... values) {
        _ARR_PARAMS.put(name, values);
        return this;
    }

    public DynamicMixinAnnotation setAnnotationParam(String name, DynamicMixinAnnotation value) {
        _ANNOTATION_PARAMS.put(name, value);
        return this;
    }

    public DynamicMixinAnnotation setAnnotationArrayParam(String name, DynamicMixinAnnotation... value) {
        _ANNOTATION_ARR_PARAMS.put(name, value);
        return this;
    }

    String getDescriptor() {
        return _TYPE.getDescriptor();
    }

    boolean isVisible() {
        return _VISIBLE;
    }

    AnnotationVisitor writeParams(AnnotationVisitor v) {
        for (Entry<String, Object> param : _NORMAL_PARAMS.entrySet()) {
            v.visit(param.getKey(), param.getValue());
        }
        for (Entry<String, Object[]> param : _ARR_PARAMS.entrySet()) {
            AnnotationVisitor array = v.visitArray(param.getKey());
            for (Object obj : param.getValue())
                array.visit(null, obj);
            array.visitEnd();
        }
        for (Entry<String, DynamicMixinAnnotation> param : _ANNOTATION_PARAMS.entrySet()) {
            AnnotationVisitor subV = v.visitAnnotation(param.getKey(), param.getValue().getDescriptor());
            param.getValue().writeParams(subV);
            subV.visitEnd();
        }
        for (Entry<String, DynamicMixinAnnotation[]> param : _ANNOTATION_ARR_PARAMS.entrySet()) {
            AnnotationVisitor array = v.visitArray(param.getKey());
            for (DynamicMixinAnnotation annotation : param.getValue()) {
                AnnotationVisitor subV = array.visitAnnotation(null, annotation.getDescriptor());
                annotation.writeParams(subV);
                subV.visitEnd();
            }
            array.visitEnd();
        }
        return v;
    }
}
