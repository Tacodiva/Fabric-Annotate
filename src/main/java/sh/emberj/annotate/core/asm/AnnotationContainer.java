package sh.emberj.annotate.core.asm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.objectweb.asm.Type;

public class AnnotationContainer {
    protected List<AnnotationMetadata> _ANNOTATIONS;

    public AnnotationContainer() {
        _ANNOTATIONS = new ArrayList<>();
    }

    void addAnnotation(AnnotationMetadata meta) {
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

    public AnnotationMetadata[] getAnnotationsByType(Class<?> clazz) {
        return getAnnotationsByType(Type.getType(clazz));
    }

    public AnnotationMetadata[] getAnnotationsByType(Type type) {
        return streamAnnotationsByType(type).toArray(AnnotationMetadata[]::new);
    }

    public AnnotationMetadata getAnnotationByType(Class<?> clazz) {
        return getAnnotationByType(Type.getType(clazz));
    }

    public AnnotationMetadata getAnnotationByType(Type type) {
        return streamAnnotationsByType(type).findAny().orElse(null);
    }

    public Stream<AnnotationMetadata> streamAnnotationsByType(Type type) {
        return _ANNOTATIONS.stream().filter(annotation -> annotation.getType().equals(type));
    }

    public Iterable<AnnotationMetadata> getAnnotations() {
        return _ANNOTATIONS;
    }

}
