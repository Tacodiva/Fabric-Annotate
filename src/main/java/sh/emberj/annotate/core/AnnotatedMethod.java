package sh.emberj.annotate.core;

import java.lang.reflect.Method;

public class AnnotatedMethod {
    
    private final AnnotatedMod _MOD;
    private final Method _METHOD;

    public AnnotatedMethod(Method method) {
        _METHOD = method;
        _MOD = Annotate.getInstance().findModFromPackage(method.getDeclaringClass().getPackageName());
        _MOD.addMethod(this);
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