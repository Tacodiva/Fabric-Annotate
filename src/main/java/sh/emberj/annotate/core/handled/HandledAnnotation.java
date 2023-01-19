package sh.emberj.annotate.core.handled;

import org.objectweb.asm.Type;

import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateMod;
import sh.emberj.annotate.core.AnnotatedClass;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.BaseAnnotation;
import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.core.ILoadListener;
import sh.emberj.annotate.core.Utils;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadata;

public class HandledAnnotation extends BaseAnnotation {

    private final ClassMetadata _CLASS;
    private final Type _HANDLER_TYPE;
    private final int _PRIORITY;
    private final AnnotateLoadStage _LOAD_STAGE;

    private Object _handlerInstance;

    public HandledAnnotation(AnnotationMetadata annotation, ClassMetadata class_,
            AnnotateMod mod) throws AnnotateException {
        super(class_, mod);
        _CLASS = class_;
        _HANDLER_TYPE = annotation.getClassParam("value");
        _PRIORITY = annotation.getIntParam("priority", 0);
        _LOAD_STAGE = annotation.getEnumParam("stage", AnnotateLoadStage.class, AnnotateLoadStage.INIT);
    }

    public Object getHandler() throws AnnotateException {
        if (_handlerInstance != null)
            return _handlerInstance;
        try {
            return _handlerInstance = Utils.instantiate(Utils.loadClass(_HANDLER_TYPE));
        } catch (AnnotateException e) {
            throw setExceptionBlame(e);
        }
    }

    public AnnotateException setExceptionBlame(AnnotateException e) {
        e.setMod(getMod().getFabricMod());
        e.setClass(_CLASS.getType());
        return e;
    }

    @Override
    public void handleInstance(AnnotationMetadata instance, AnnotatedClass annotatedClass) {
        Annotate.addLoadListener(new ClassLoadListener(instance, annotatedClass));
    }

    @Override
    public void handleInstance(AnnotationMetadata instance, AnnotatedMethod annotatedMethod) {
        Annotate.addLoadListener(new MethodLoadListener(instance, annotatedMethod));
    }

    private abstract class BaseLoadListener implements ILoadListener {
        @Override
        public int getPriority() {
            return _PRIORITY;
        }

        @Override
        public AnnotateLoadStage getLoadStage() {
            return _LOAD_STAGE;
        }
    }

    private class ClassLoadListener extends BaseLoadListener {
        public final AnnotatedClass CLASS;
        public final AnnotationMetadata ANNOTATION;

        public ClassLoadListener(AnnotationMetadata annotation, AnnotatedClass class_) {
            CLASS = class_;
            ANNOTATION = annotation;
        }

        @Override
        public void onLoad() throws AnnotateException {
            Object handlerObj = getHandler();
            if (!(handlerObj instanceof IClassAnnotationHandler handler))
                throw setExceptionBlame(
                        new AnnotateException("Annotaiton handler of type " + handlerObj.getClass()
                                + " must implement IClassAnnotationHandler."));
            try {
                handler.handleClassAnnotation(CLASS, ANNOTATION);
            } catch (Exception e) {
                AnnotateException ae;
                if (e instanceof AnnotateException cast)
                    ae = cast;
                else
                    ae = new AnnotateException("Unknown error while handling class annotation.", e);
                ae.trySet(CLASS);
                throw ae;
            }
        }
    }

    private class MethodLoadListener extends BaseLoadListener {
        public final AnnotatedMethod METHOD;
        public final AnnotationMetadata ANNOTATION;

        public MethodLoadListener(AnnotationMetadata annotation, AnnotatedMethod method) {
            METHOD = method;
            ANNOTATION = annotation;
        }

        @Override
        public void onLoad() throws AnnotateException {
            Object handlerObj = getHandler();
            if (!(handlerObj instanceof IMethodAnnotationHandler handler))
                throw setExceptionBlame(
                        new AnnotateException("Annotaiton handler of type " + handlerObj.getClass()
                                + " must implement IMethodAnnotationHandler."));
            try {
                handler.handleMethodAnnotation(METHOD, ANNOTATION);
            } catch (Exception e) {
                AnnotateException ae;
                if (e instanceof AnnotateException cast)
                    ae = cast;
                else
                    ae = new AnnotateException("Unknown error while handling method annotation.", e);
                ae.trySet(METHOD);
                throw ae;
            }
        }
    }
}
