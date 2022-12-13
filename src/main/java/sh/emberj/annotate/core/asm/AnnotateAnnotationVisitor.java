package sh.emberj.annotate.core.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

class AnnotateAnnotationVisitor extends AnnotationVisitor implements Opcodes {
    private final AnnotationMeta _TARGET;

    protected AnnotateAnnotationVisitor(AnnotationMeta meta) {
        super(ASM9);
        _TARGET = meta;
    }

    @Override
    public void visit(String name, Object value) {
        _TARGET.addParam(name, value);
    }

    @Override
    public void visitEnum(final String name, final String descriptor, final String value) {
        _TARGET.addEnumParam(name, new EnumValueMeta(descriptor, value));
    }
}