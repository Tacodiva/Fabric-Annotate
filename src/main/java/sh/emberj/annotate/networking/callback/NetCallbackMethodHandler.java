package sh.emberj.annotate.networking.callback;

import net.minecraft.network.NetworkSide;
import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateIdentifier;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.handled.IMethodAnnotationHandler;

public class NetCallbackMethodHandler implements IMethodAnnotationHandler {

    @Override
    public void handleMethodAnnotation(AnnotatedMethod method, AnnotationMetadata annotation) throws AnnotateException {
        Identifier id = AnnotateIdentifier.createIdentifier(annotation.getStringParam("path"),
                annotation.getStringParam("namespace"), method);
        NetCallbackRegistry.INSTANCE.register(id,
                new NetCallbackInfo(id, annotation.getEnumParam("value", NetworkSide.class),
                        annotation.getBooleanParam("executeAsync", false), method));
    }

}
