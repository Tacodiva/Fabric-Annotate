package sh.emberj.annotate.core;

import java.lang.reflect.Method;

import sh.emberj.annotate.core.asm.AnnotatedMethodMeta;

public class AnnotatedMethod {
    
    private final AnnotatedMod _MOD;
    private final AnnotatedMethodMeta _META;
    
    private Method _method;

    public AnnotatedMethod(AnnotatedMethodMeta method) {
        // _METHOD = method.getReturnType();
        _META = method;
        _MOD = Annotate.getInstance().findModFromPackage(getDeclaringClass().getPackageName());
        _MOD.addMethod(this);
    }

    public AnnotatedMethodMeta getMeta() {
        return _META;
    }

    public AnnotatedMod getMod() {
        return _MOD;
    }

    public Class<?> getDeclaringClass() {
        return _META.getDeclaringType().getAsClass();
    }

    public Method getMethod() throws AnnotateException {
        return getMethod(false);
    }

    public Method getMethod(boolean force) throws AnnotateException {
        if (_method != null) return _method;
        LoadStage stage = Annotate.getLoadStage();
        if (!force && (stage == null || stage == LoadStage.PRELAUNCH))
            throw new AnnotateException("Should not load method during load stage " + stage + "!");
        return _method = _META.getMethod();
    }

    public String getName() {
        return _META.getName();
    }

    @Override
    public String toString() {
        return _META.toString();
    }

}