package sh.emberj.annotate.alloy;

import org.objectweb.asm.Type;

import sh.emberj.annotate.core.asm.AnnotationMetadata;

public record AlloyMethodArg(AlloyMethodArgType alloyType, AnnotationMetadata annotation, Type type) {
    
}
