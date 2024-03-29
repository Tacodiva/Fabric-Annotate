package sh.emberj.annotate.entrypoint;

import java.lang.reflect.InvocationTargetException;

import org.objectweb.asm.Opcodes;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.core.ILoadListener;

public class EntrypointInstance implements ILoadListener {

    private final AnnotateLoadStage _STAGE;
    private final AnnotatedMethod _METHOD;
    private final int _PRIORITY;

    public EntrypointInstance(AnnotateLoadStage stage, int priority, AnnotatedMethod method) throws AnnotateException {
        _STAGE = stage;
        _METHOD = method;
        _PRIORITY = priority;

        if (!_METHOD.getMetadata().hasModifier(Opcodes.ACC_STATIC))
            throw new AnnotateException("Methods annotated with @Entrypoint must be static!");
        if (_METHOD.getMetadata().getArgNum() != 0)
            throw new AnnotateException("Methods annotated with @Entrypoint must have no arguments.");
    }

    @Override
    public AnnotateLoadStage getLoadStage() {
        return _STAGE;
    }

    @Override
    public int getPriority() {
        return _PRIORITY;
    }

    @Override
    public void onLoad() throws AnnotateException {
        try {
            _METHOD.getMetadata().getMethod().invoke(null);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new AnnotateException("Encountered an unexpected exception while invoking entrypoint.", e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof AnnotateException ae)
                throw ae;
            throw new AnnotateException("Entrypoint threw an exception.", e.getCause());
        }
    }

    public AnnotatedMethod getMethod() {
        return _METHOD;
    }

}
