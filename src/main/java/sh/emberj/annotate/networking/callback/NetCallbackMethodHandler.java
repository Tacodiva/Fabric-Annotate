package sh.emberj.annotate.networking.callback;

import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateIdentifier;
import sh.emberj.annotate.core.AnnotateScan;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.AnnotatedMethodHandler;
import sh.emberj.annotate.core.LoadStage;

@AnnotateScan
public class NetCallbackMethodHandler extends AnnotatedMethodHandler {

    public NetCallbackMethodHandler() {
        super(LoadStage.PRELAUNCH);
    }

    @Override
    public void handle(AnnotatedMethod method) throws AnnotateException {
        NetCallback annotation = tryGetAnnotation(method, NetCallback.class, true);
        if (annotation == null)
            return;
        Identifier id = AnnotateIdentifier.createIdentifier(annotation.path(), annotation.namespace(), method);
        NetCallbackRegistry.INSTANCE.register(id, new NetCallbackInfo(id, annotation.value(), annotation.executeAsync(), method));
    }

}
