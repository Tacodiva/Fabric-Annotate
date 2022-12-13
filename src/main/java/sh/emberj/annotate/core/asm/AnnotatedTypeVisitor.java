package sh.emberj.annotate.core.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

class AnnotatedTypeVisitor extends ClassVisitor implements Opcodes {
    private final Type _TYPE;
    private AnnotatedTypeMeta _target;

    protected AnnotatedTypeVisitor(Type type) {
        super(ASM9);
        _TYPE = type;
    }

    public AnnotatedTypeMeta getTarget() {
        return _target;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        _target = new AnnotatedTypeMeta(_TYPE, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor,
            final boolean visible) {
        AnnotationMeta annotation = new AnnotationMeta(Type.getType(descriptor));
        _target.addAnnotation(annotation);
        return new AnnotateAnnotationVisitor(annotation);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        AnnotatedMethodMeta method = new AnnotatedMethodMeta(_target, name, desc, access, exceptions);
        _target.addMethod(method);
        return new AnnotatedMethodVisitor(method);
    }

}
