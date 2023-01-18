package sh.emberj.annotate.alloy;

import java.lang.annotation.Annotation;

import org.objectweb.asm.MethodVisitor;

import sh.emberj.annotate.core.AnnotateException;

public abstract class AlloyArgumentType {

    private final Class<? extends Annotation> _ANNOTATION;

    public AlloyArgumentType(Class<? extends Annotation> annotation) {
        _ANNOTATION = annotation;
    }

    public Class<? extends Annotation> getAnnotation() {
        return _ANNOTATION;
    }

    public boolean requireCancellable(AlloyArgument argument) {
        return false;
    }

    public abstract void generateASM(MethodVisitor mw, AlloyArgument arg, IAlloyMethod method)
            throws AnnotateException;
}
