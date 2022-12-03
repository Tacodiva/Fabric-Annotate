package sh.emberj.annotate.registry;

import net.minecraft.util.Identifier;
import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateIdentifier;
import sh.emberj.annotate.core.AnnotateScan;
import sh.emberj.annotate.core.AnnotatedTypeHandler;
import sh.emberj.annotate.core.AnnotatedType;
import sh.emberj.annotate.core.LoadStage;

@AnnotateScan
public class RegisterTypeHandler extends AnnotatedTypeHandler {

    public RegisterTypeHandler() {
        super(null);
    }

    @Override
    public void handle(AnnotatedType type) throws AnnotateException {
        Register[] annotations = tryGetAnnotations(type, Register.class);
        final LoadStage currentStage = Annotate.getLoadStage();

        for (Register annotation : annotations) {
            if (annotation.loadStage() != currentStage) continue;

            Identifier identifier = null;

            if (annotation.path().isBlank() && annotation.namespace().isBlank()) {
                IIdentifiable instance = tryCastInstance(type, IIdentifiable.class);
                if (instance != null) identifier = instance.getIdentifier();
            }

            if (identifier == null) {
                identifier = AnnotateIdentifier.createIdentifier(annotation.namespace(), annotation.path(), type);
            }

            Identifier registryIdentifier = AnnotateIdentifier.createIdentifier(annotation.registry(), type);
            RegistryManager.register(registryIdentifier, identifier, getInstance(type));
        }
    }

}
