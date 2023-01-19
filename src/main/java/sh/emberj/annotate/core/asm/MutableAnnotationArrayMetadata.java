package sh.emberj.annotate.core.asm;

import org.objectweb.asm.Type;

public class MutableAnnotationArrayMetadata extends AnnotationArrayMetadata {

    public void addClass(Type value) {
        _CONTENTS.add(value);
    }

    public void addString(String value) {
        _CONTENTS.add(value);
    }

    public void addInt(int value) {
        _CONTENTS.add(value);
    }

    public void addEnum(AnnotationEnumMetadata value) {
        _CONTENTS.add(value);
    }

    public void addArray(AnnotationArrayMetadata value) {
        _CONTENTS.add(value);
    }

    public void addAnnotation(AnnotationMetadata value) {
        _CONTENTS.add(value);
    }
}
