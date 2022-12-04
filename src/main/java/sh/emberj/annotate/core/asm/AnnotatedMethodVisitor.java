package sh.emberj.annotate.core.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

class AnnotatedMethodVisitor extends MethodVisitor implements Opcodes {
    private final AnnotatedMethodMeta _TARGET;

    protected AnnotatedMethodVisitor(AnnotatedMethodMeta method) {
        super(ASM9);
        _TARGET = method;
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
        AnnotationMeta annotation = new AnnotationMeta(Type.getType(descriptor));
        _TARGET.addAnnotation(annotation);
        return new AnnotateAnnotationVisitor(annotation);
    }
}
