package sh.emberj.annotate.mixin;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.Mixin;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.MutableAnnotationArrayMetadata;
import sh.emberj.annotate.core.asm.MutableAnnotationMetadata;

public class DynamicMixinClass implements Opcodes {

    private final Type _TARGET;
    private final String _CLASS_NAME;
    private final List<IDynamicMixinMethodGenerator> _METHODS;

    public DynamicMixinClass(Type target, String className) {
        _TARGET = target;
        _METHODS = new ArrayList<>();
        _CLASS_NAME = className;
    }

    public void addMethod(IDynamicMixinMethodGenerator methodGenerator) throws AnnotateException {
        if (!methodGenerator.getTargetType().equals(_TARGET))
            throw new IllegalArgumentException(
                    "methodGenerator must target the same type as the class it's being added to.");
        _METHODS.add(methodGenerator);
    }

    public Type getTarget() {
        return _TARGET;
    }

    public String getClassName() {
        return _CLASS_NAME;
    }

    public byte[] generateBytecode() throws AnnotateException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        {
            MutableAnnotationMetadata mixinAnnotation = new MutableAnnotationMetadata(Mixin.class);
            MutableAnnotationArrayMetadata value = new MutableAnnotationArrayMetadata();
            value.addClass(_TARGET);
            mixinAnnotation.setArrayParam("value", value);
            mixinAnnotation.write(cw.visitAnnotation(mixinAnnotation.getType().getDescriptor(), false));
        }

        cw.visit(V1_1, ACC_PUBLIC | ACC_SUPER, _CLASS_NAME, null, "java/lang/Object", null);

        {
            MethodVisitor constructor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);

            // pushes the 'this' variable
            constructor.visitVarInsn(ALOAD, 0);
            // invokes the super class constructor
            constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            constructor.visitInsn(RETURN);
            // this code uses a maximum of one stack element and one local variable
            constructor.visitMaxs(1, 1);
            constructor.visitEnd();
        }

        for (int i = 0; i < _METHODS.size(); i++) {
            final IDynamicMixinMethodGenerator method = _METHODS.get(i);
            MethodVisitor mw = cw.visitMethod(method.generateMethodFlags(), method.getNamePrefix() + i,
                    method.generateDescriptor(), null,
                    null);
            AnnotationMetadata methodAnnotation = method.generateAnnotation();
            methodAnnotation.write(mw.visitAnnotation(methodAnnotation.getType().getDescriptor(), true));
            method.generateMethod(mw);
            mw.visitEnd();
        }

        cw.visitEnd();
        return cw.toByteArray();
    }
}
