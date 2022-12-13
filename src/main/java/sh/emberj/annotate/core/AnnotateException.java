package sh.emberj.annotate.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;

import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.gui.FabricGuiEntry;
import net.fabricmc.loader.impl.gui.FabricStatusTree;
import net.fabricmc.loader.impl.gui.FabricStatusTree.FabricBasicButtonType;
import net.fabricmc.loader.impl.gui.FabricStatusTree.FabricStatusTab;
import net.fabricmc.loader.impl.gui.FabricStatusTree.FabricTreeWarningLevel;

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
        this(cause, null, type.getAsClass(), type.getMod().getModContainer(), null);
    }

    public AnnotateException(String cause, AnnotatedType type, Throwable e) {
        this(cause, null, type.getAsClass(), type.getMod().getModContainer(), e);
    }

    public AnnotateException(String cause, AnnotatedMethod method) {
        this(cause, method.getName(), method.getDeclaringClass(), method.getMod().getModContainer(), null);
    }

    public AnnotateException(String cause, AnnotatedMethod method, Throwable e) {
        this(cause, method.getName(), method.getDeclaringClass(), method.getMod().getModContainer(), e);
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
        this(cause, member, type.getAsClass(), type.getMod().getModContainer(), null);
    }

    public AnnotateException(String cause, String member, AnnotatedType type, Throwable e) {
        this(cause, member, type.getAsClass(), type.getMod().getModContainer(), e);
    }

    public AnnotateException(String cause, String member, Class<?> clazz, ModContainer mod, Throwable e) {
        super(cause, e);
        this._problemMember = member;
        this._problemClass = clazz;
        this._problemMod = mod;
    }

    public void trySetMod(ModContainer mod) {
        if (_problemMod == null)
            _problemMod = mod;
    }

    public void trySetClass(Class<?> clazz) {
        if (_problemClass == null)
            _problemClass = clazz;
    }

    public void trySetMember(String member) {
        if (_problemMember == null)
            _problemMember = member;
    }

    public void trySet(AnnotatedType type) {
        trySetClass(type.getAsClass());
        trySetMod(type.getMod().getModContainer());
    }

    public void trySet(AnnotatedMethod method) {
        trySetMember(method.getName());
        trySetClass(method.getDeclaringClass());
        trySetMod(method.getMod().getModContainer());
    }

    public void showGUI() {
        System.err.println();
        if (getCause() == null)
            printStackTrace();
        else
            getCause().printStackTrace();
        System.err.println();

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
            Annotate.LOG.error("Culprit Mod: Unknown");
        if (_problemClass != null)
            Annotate.LOG.error("Culprit Class: " + _problemClass.getName());
        if (_problemMember != null)
            Annotate.LOG.error("Culprit Member: " + _problemMember);
        if (getCause() != null)
            Annotate.LOG.error(ExceptionUtils.getStackTrace(getCause()));
        Annotate.LOG.error("");
        Annotate.LOG.error("====================================");

        FabricGuiEntry.displayError("Annotate encountered an error!", this, tree -> {

            FabricStatusTab tabCrash = tree.tabs.get(0);
            tabCrash.node.name = "Exception";

            FabricStatusTab tabErr = tree.addTab("Report");
            tabErr.addChild("");
            tabErr.addChild(message).setWarningLevel(FabricTreeWarningLevel.WARN);
            tabErr.addChild("");
            tabErr.addChild(getMessage()).setWarningLevel(FabricTreeWarningLevel.ERROR);
            tabErr.addChild("");

            if (_problemMod != null)
                tabErr.addChild("Culprit Mod: " + _problemMod.getMetadata().getName() + " ("
                        + _problemMod.getMetadata().getId() + ")").iconType = FabricStatusTree.ICON_TYPE_JAR_FILE;
            else
                tabErr.addChild("Culprit Mod: Unknown").iconType = FabricStatusTree.ICON_TYPE_JAR_FILE;

            if (_problemClass != null)
                tabErr.addChild(
                        "Culprit Class: " + _problemClass.getName()).iconType = FabricStatusTree.ICON_TYPE_JAVA_CLASS;
            if (_problemMember != null)
                tabErr.addChild(
                        "Culprit Member: " + _problemMember).iconType = FabricStatusTree.ICON_TYPE_JAVA_CLASS;
            tabErr.addChild("");
            if (getCause() != null)
                tabErr.addChild("Error Caused by Exception").addException(getCause());

            tree.tabs.set(0, tabErr);
            tree.tabs.set(1, tabCrash);

            StringWriter error = new StringWriter();
            error.append("\n");
            error.append("Error caught and modified by " + Annotate.getBranding());
            error.append("\n");
            error.append(new Date().toString());
            error.append("\n");
            error.append("\n");
            error.append("Load Stage: ");
            error.append(Annotate.getLoadStage().toString());
            if (_problemMod != null)
                error.append("\nCulprit Mod: " + _problemMod.getMetadata().getName() + " ("
                        + _problemMod.getMetadata().getId() + ")");
            if (_problemClass != null)
                error.append("\nCulprit Class: " + _problemClass.getName());
            if (_problemMember != null)
                error.append("\nCulprit Member: " + _problemMember);
            if (getCause() != null)
                error.append("\n" + ExceptionUtils.getStackTrace(getCause()));
            error.append("\n");

            error.append(System.lineSeparator());
            printStackTrace(new PrintWriter(error));
            error.append("\n");

            tree.addButton("Copy error", FabricBasicButtonType.CLICK_MANY).withClipboard(error.toString());
        }, true);
        throw new AssertionError("Above calls System.exit() so this should never happen.");
    }
}
