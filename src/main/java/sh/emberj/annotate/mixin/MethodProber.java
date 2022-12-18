package sh.emberj.annotate.mixin;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.asm.AnnotatedMethodMeta;
import sh.emberj.annotate.mixin.asm.DynamicMixinAnnotation;
import sh.emberj.annotate.mixin.asm.IDynamicMixinMethodGenerator;

public class MethodProber {
    private MethodProber() {
    }

    private static final String ASM_NAME = MethodProber.class.getCanonicalName();

    private static boolean isProbing;
    private static String probeResults;

    private static final String ASM_IS_PROBING_NAME = "isProbing";
    private static final String ASM_IS_PROBING_DESC = "()Z";

    public static boolean isProbing() {
        return isProbing;
    }

    private static final String ASM_SET_PROBE_RESULT_NAME = "setProbeResults";
    private static final String ASM_SET_PROBE_RESULT_DESC = "(Ljava/lang/String;)V";

    public static void setProbeResults(String value) {
        if (!isProbing)
            throw new IllegalStateException("Cannot set probe results as we are not currenctly probing.");
        if (probeResults != null)
            throw new IllegalStateException("Cannot set probe results multiple times.");
        probeResults = value;
    }

    public static void setupProbe(AnnotatedMethodMeta target, String value) throws AnnotateException {
        AnnotateMixins.addMixin(new MethodProbeGenerator(target, value));
    }

    public static String probe(Runnable target) throws AnnotateException {
        return probe(target, false);
    }

    public static String probe(Runnable target, boolean allowFailure) throws AnnotateException {
        if (isProbing())
            throw new IllegalStateException("Already probing something!");
        probeResults = null;
        isProbing = true;
        target.run();
        if (!allowFailure && probeResults == null)
            throw new AnnotateException("Probe failed! setProbeResults() was never called.");
        isProbing = false;
        return probeResults;
    }

    private static class MethodProbeGenerator implements IDynamicMixinMethodGenerator {
        private final AnnotatedMethodMeta _TARGET;
        private final String _VALUE;
    
        public MethodProbeGenerator(AnnotatedMethodMeta target, String value) {
            _TARGET = target;
            _VALUE = value;
        }
    
        @Override
        public DynamicMixinAnnotation generateAnnotation() {
            DynamicMixinAnnotation inject = new DynamicMixinAnnotation(Inject.class, true);
    
            DynamicMixinAnnotation at = new DynamicMixinAnnotation(At.class, true);
            at.setParam("value", "HEAD");
            inject.setAnnotationArrayParam("at", at);
            inject.setArrayParam("method", _TARGET.getName() + _TARGET.getDescriptor());
            inject.setParam("cancellable", true);
            return inject;
        }
    
        @Override
        public String generateDescriptor() {
            return Type.getMethodDescriptor(Type.VOID_TYPE, TYPE_CALLBACK_INFO);
        }
    
        @Override
        public int generateMethodFlags() {
            return ACC_PRIVATE | ACC_STATIC;
        }
    
        @Override
        public void generateMethod(MethodVisitor mw) {
            // if (MethodProber.isProbing()) {
            mw.visitMethodInsn(INVOKESTATIC, MethodProber.ASM_NAME, MethodProber.ASM_IS_PROBING_NAME,
                    MethodProber.ASM_IS_PROBING_DESC, false);
            Label returnLabel = new Label();
            mw.visitJumpInsn(IFEQ, returnLabel);
    
            // MethodProber.setProbeResults(_VALUE);
            mw.visitLdcInsn(_VALUE);
            mw.visitMethodInsn(INVOKESTATIC, MethodProber.ASM_NAME, MethodProber.ASM_SET_PROBE_RESULT_NAME,
                    MethodProber.ASM_SET_PROBE_RESULT_DESC, false);
    
            // callbackInfo.cancel()
            mw.visitVarInsn(ALOAD, 0);
            mw.visitMethodInsn(INVOKEVIRTUAL, TYPE_CALLBACK_INFO.getInternalName(), "cancel", "()V", false);
    
            // }
            mw.visitLabel(returnLabel);
    
            // return;
            mw.visitInsn(RETURN);
        }
    
        @Override
        public String getNamePrefix() {
            return "methodProbe";
        }
    
        @Override
        public Type getTargetType() {
            return _TARGET.getDeclaringType().getType();
        }
    
    }
}
