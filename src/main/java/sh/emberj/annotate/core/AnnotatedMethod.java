package sh.emberj.annotate.core;

import java.lang.reflect.Method;

import sh.emberj.annotate.core.asm.AnnotatedMethodMeta;
import sh.emberj.annotate.core.asm.AnnotatedTypeMeta;

public class AnnotatedMethod {
    
    private final AnnotatedMod _MOD;
    private final Method _METHOD;

    private AnnotatedMethodMeta _meta;

    public AnnotatedMethod(Method method) {
        _METHOD = method;
        _MOD = Annotate.getInstance().findModFromPackage(method.getDeclaringClass().getPackageName());
        _MOD.addMethod(this);
    }

    public AnnotatedMethodMeta getMeta() throws AnnotateException {
        if (_meta != null) return _meta;
        return _meta = AnnotatedTypeMeta.readMetadata(_METHOD.getDeclaringClass()).getMethod(_METHOD);
    }

    public AnnotatedMod getMod() {
        return _MOD;
    }

    public Method getMethod() {
        return _METHOD;
    }

    @Override
    public String toString() {
        return _METHOD.toString();
    }

}