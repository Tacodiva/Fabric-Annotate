package sh.emberj.annotate.core.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

class AnnotatedTypeVisitor extends ClassVisitor implements Opcodes {

    private final AnnotatedTypeMeta _TARGET;

    protected AnnotatedTypeVisitor(AnnotatedTypeMeta meta) {
        super(ASM9);
        _TARGET = meta;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor,
            final boolean visible) {
        AnnotationMeta annotation = new AnnotationMeta(Type.getType(descriptor));
        _TARGET.addAnnotation(annotation);
        return new AnnotateAnnotationVisitor(annotation);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        AnnotatedMethodMeta method = new AnnotatedMethodMeta(_TARGET, name, desc, access, exceptions);
        _TARGET.addMethod(method);
        return new AnnotatedMethodVisitor(method);
    }

}
