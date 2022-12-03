package sh.emberj.annotate.core;

import org.apache.commons.lang3.exception.ExceptionUtils;

import net.fabricmc.loader.api.ModContainer;

public class AnnotateException extends Exception {

    private ModContainer _problemMod;
    private Class<?> _problemClass;
    private String _problemMember;

    public AnnotateException() {
        this(null, null, null, null, null);
    }

    public AnnotateException(String cause) {
        this(cause, null, null, null, null);
    }

    public AnnotateException(String cause, Exception e) {
        this(cause, null, null, null, e);
    }

    public AnnotateException(String cause, AnnotatedType type) {
        this(cause, null, type.getRawType(), type.getMod().getModContainer(), null);
    }

    public AnnotateException(String cause, AnnotatedType type, Throwable e) {
        this(cause, null, type.getRawType(), type.getMod().getModContainer(), e);
    }

    public AnnotateException(String cause, AnnotatedMethod method) {
        this(cause, method.getMethod().getName(), method.getClass(), method.getMod().getModContainer(), null);
    }

    public AnnotateException(String cause, AnnotatedMethod method, Throwable e) {
        this(cause, method.getMethod().getName(), method.getClass(), method.getMod().getModContainer(), e);
    }

    public AnnotateException(String cause, AnnotatedMod mod) {
        this(cause, null, null, mod.getModContainer(), null);
    }

    public AnnotateException(String cause, AnnotatedMod mod, Throwable e) {
        this(cause, null, null, mod.getModContainer(), e);
    }

    public AnnotateException(String cause, ModContainer mod) {
        this(cause, null, null, mod, null);
    }

    public AnnotateException(String cause, ModContainer mod, Throwable e) {
        this(cause, null, null, mod, e);
    }

    public AnnotateException(String cause, String member, AnnotatedType type) {
        this(cause, member, type.getRawType(), type.getMod().getModContainer(), null);
    }

    public AnnotateException(String cause, String member, AnnotatedType type, Throwable e) {
        this(cause, member, type.getRawType(), type.getMod().getModContainer(), e);
    }

    public AnnotateException(String cause, String member, Class<?> clazz, ModContainer mod, Throwable e) {
        super(cause, e);
        this._problemMember = member;
        this._problemClass = clazz;
        this._problemMod = mod;
    }

    public void trySetMod(ModContainer mod) {
        if (_problemMod == null) _problemMod = mod;
    }

    public void trySetClass(Class<?> clazz) {
        if (_problemClass == null) _problemClass = clazz;
    }

    public void trySetMember(String member) {
        if (_problemMember == null) _problemMember = member;
    }

    public void trySet(AnnotatedType type) {
        trySetClass(type.getRawType());
        trySetMod(type.getMod().getModContainer());
    }

    public void trySet(AnnotatedMethod method) {
        trySetMember(method.getMethod().getName());
        trySetClass(method.getClass());
        trySetMod(method.getMod().getModContainer());
    }

    public RuntimeException rethrow() {
        String message;
        if (getCause() == null)
            message = "Encountered an annotate exception during load stage " + Annotate.getLoadStage();
        else
            message = "Encountered an unexpected " + getCause().getClass().getSimpleName()
                    + " exception during load stage " + Annotate.getLoadStage();
        Annotate.LOG.error("====================================");
        Annotate.LOG.error("");
        Annotate.LOG.error(message);
        Annotate.LOG.error("Error: " + getMessage());
        if (_problemMod != null)
            Annotate.LOG.error("Culprit Mod: " + _problemMod.getMetadata().getName() + " ("
                    + _problemMod.getMetadata().getId() + ")");
        else
            Annotate.LOG.error("Culprit Mod: ???");
        if (_problemClass != null) Annotate.LOG.error("Culprit Class: " + _problemClass.getName());
        if (_problemMember != null) Annotate.LOG.error("Culprit Class Member: " + _problemMember);
        if (getCause() != null) Annotate.LOG.error(ExceptionUtils.getStackTrace(getCause()));
        Annotate.LOG.error("");
        Annotate.LOG.error("====================================");
        if (getCause() == null)
            return new RuntimeException("Encountered an AnnotateException. Check logs for details.", this);
        else
            return new RuntimeException("Annotate encountered an unexpected exception. Check logs for details.",
                    getCause());
    }
}
