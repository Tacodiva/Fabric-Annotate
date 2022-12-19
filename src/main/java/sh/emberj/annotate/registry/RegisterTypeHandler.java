package sh.emberj.annotate.registry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateIdentifier;
import sh.emberj.annotate.core.AnnotateScan;
import sh.emberj.annotate.core.AnnotatedType;
import sh.emberj.annotate.core.AnnotatedTypeHandler;
import sh.emberj.annotate.core.LoadStage;
import sh.emberj.annotate.core.asm.AnnotationMeta;

@AnnotateScan
public class RegisterTypeHandler extends AnnotatedTypeHandler {

    public static record RegisterAnnotationInfo(Class<?> annotation, String registry, LoadStage loadStage) {
    }

    private static final List<RegisterAnnotationInfo> _REGISTER_ANNOTATION_INFO = new ArrayList<>();

    static void addInfo(RegisterAnnotationInfo info) {
        _REGISTER_ANNOTATION_INFO.add(info);
    }

    public RegisterTypeHandler() {
        super(null, -190);
    }

    @Override
    public void handle(AnnotatedType type) throws AnnotateException {
        Register[] annotations = tryGetAnnotations(type, Register.class);
        final LoadStage currentStage = Annotate.getLoadStage();

        for (Register annotation : annotations) {
            if (annotation.loadStage() != currentStage)
                continue;
            register(type, annotation.registry(), annotation.path(), annotation.namespace());
        }

        for (RegisterAnnotationInfo info : _REGISTER_ANNOTATION_INFO) {
            if (info.loadStage() != currentStage)
                continue;
            AnnotationMeta annotation = type.getMeta().getAnnotationByType(info.annotation());
            if (annotation == null)
                continue;
            String path = annotation.getStringParam("path");
            String namespace = annotation.getStringParam("namespace");
            register(type, info.registry, path, namespace);
        }
    }

    private void register(AnnotatedType type, String registry, String path, String namespace) throws AnnotateException {
        Identifier identifier = null;

        if ((path == null || path.isBlank()) && (namespace == null || namespace.isBlank())) {
            IIdentifiable instance = tryCastInstance(type, IIdentifiable.class);
            if (instance != null)
                identifier = instance.getIdentifier();
        }

        if (identifier == null)
            identifier = AnnotateIdentifier.createIdentifier(namespace, path, type);

        Identifier registryIdentifier = AnnotateIdentifier.createIdentifier(registry, type);
        RegistryManager.register(registryIdentifier, identifier, type.getInstance());
    }
}
