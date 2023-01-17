package sh.emberj.annotate.core;

import sh.emberj.annotate.core.asm.MethodMetadata;

public class AnnotatedMethod {

    private final MethodMetadata _METADATA;
    private final AnnotateMod _MOD;

    public AnnotatedMethod(AnnotateMod mod, MethodMetadata metadata) {
        _METADATA = metadata;
        _MOD = mod;
    }

    public MethodMetadata getMetadata() {
        return _METADATA;
    }

    public AnnotateMod getMod() {
        return _MOD;
    }
}
