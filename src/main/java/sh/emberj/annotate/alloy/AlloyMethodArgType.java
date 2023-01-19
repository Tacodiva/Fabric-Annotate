package sh.emberj.annotate.alloy;

import java.lang.annotation.Annotation;

import org.objectweb.asm.tree.InsnList;

import sh.emberj.annotate.alloy.mixinext.AlloyInjector;
import sh.emberj.annotate.core.AnnotateException;

public abstract class AlloyMethodArgType {

    private final Class<? extends Annotation> _ANNOTATION;

    public AlloyMethodArgType(Class<? extends Annotation> annotation) {
        _ANNOTATION = annotation;
    }

    public Class<? extends Annotation> getAnnotation() {
        return _ANNOTATION;
    }

    public abstract void preInject(AlloyInjector injector, AlloyMethodArg arg, IAlloyInjection injection)
            throws AnnotateException;

    public abstract void inject(InsnList asm, AlloyInjector injector, AlloyMethodArg arg, IAlloyInjection injection)
            throws AnnotateException;
}
