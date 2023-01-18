package sh.emberj.annotate.alloy;

import org.objectweb.asm.Type;

import sh.emberj.annotate.core.asm.AnnotationMetadata;

public record AlloyArgument(AlloyArgumentType alloyType, AnnotationMetadata annotation, Type type) {
    
}
