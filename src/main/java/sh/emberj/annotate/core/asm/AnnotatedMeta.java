package sh.emberj.annotate.core.asm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.objectweb.asm.Type;

public abstract class AnnotatedMeta {
    protected List<AnnotationMeta> _ANNOTATIONS;

    public AnnotatedMeta() {
        _ANNOTATIONS = new ArrayList<>();
    }

    void addAnnotation(AnnotationMeta meta) {
        _ANNOTATIONS.add(meta);
    }

    public boolean hasAnnotations() {
        return !_ANNOTATIONS.isEmpty();
    }

    public boolean hasAnnotation(Class<?> clazz) {
        return hasAnnotation(Type.getType(clazz));
    }

    public boolean hasAnnotation(Type type) {        
        return getAnnotationByType(type) != null;
    }

    public AnnotationMeta[] getAnnotationsByType(Class<?> clazz) {
        return getAnnotationsByType(Type.getType(clazz));
    }
    
    public AnnotationMeta[] getAnnotationsByType(Type type) {
        return streamAnnotationsByType(type).toArray(AnnotationMeta[]::new);
    }

    public AnnotationMeta getAnnotationByType(Class<?> clazz) {
        return getAnnotationByType(Type.getType(clazz));
    }

    public AnnotationMeta getAnnotationByType(Type type) {
        return streamAnnotationsByType(type).findAny().orElse(null);
    }

    public Stream<AnnotationMeta> streamAnnotationsByType(Type type) {
        return _ANNOTATIONS.stream().filter(annotation -> annotation.getType().equals(type));
    }

    public Iterable<AnnotationMeta> getAnnotations() {
        return _ANNOTATIONS;
    }
}
