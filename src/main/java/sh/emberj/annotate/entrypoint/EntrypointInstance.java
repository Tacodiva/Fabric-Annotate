package sh.emberj.annotate.entrypoint;

import java.lang.reflect.InvocationTargetException;

import org.objectweb.asm.Opcodes;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.LoadStage;

public class EntrypointInstance {

    private final LoadStage _STAGE;
    private final AnnotatedMethod _METHOD;
    private final int _PRIORITY;

    public EntrypointInstance(LoadStage stage, int priority, AnnotatedMethod method) throws AnnotateException {
        _STAGE = stage;
        _METHOD = method;
        _PRIORITY = priority;

        if (!_METHOD.getMeta().hasModifier(Opcodes.ACC_STATIC))
            throw new AnnotateException("Methods annotated with @Entrypoint must be static!");
        if (_METHOD.getMeta().getArgNum() != 0)
            throw new AnnotateException("Methods annotated with @Entrypoint must have no arguments.");
    }

    public LoadStage getLoadStage() {
        return _STAGE;
    }

    public AnnotatedMethod getMethod() {
        return _METHOD;
    }

    public int getPriority() {
        return _PRIORITY;
    }

    public void tryInvoke() throws AnnotateException {
        try {
            _METHOD.getMethod(true).invoke(null);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new AnnotateException("Encountered an unexpected exception while invoking entrypoint.", e);
        } catch (InvocationTargetException e) {
            throw new AnnotateException("Entrypoint threw an exception.", e);
        }
    }
}
