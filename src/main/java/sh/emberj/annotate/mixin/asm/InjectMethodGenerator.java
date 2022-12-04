package sh.emberj.annotate.mixin.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.asm.AnnotatedMethodMeta;
import sh.emberj.annotate.core.asm.AnnotatedTypeMeta;
import sh.emberj.annotate.core.asm.AnnotationMeta;

public class InjectMethodGenerator implements IDynamicMixinMethodGenerator, Opcodes {

    public static enum InjectPosition {
        HEAD("HEAD"),
        TAIL("TAIL"),
        RETURN(null);

        private final String _VALUE;

        private InjectPosition(String value) {
            _VALUE = value;
        }

        public String getValue() {
            return _VALUE;
        }
    }

    private final AnnotatedMethod _MIXIN;
    private final AnnotatedMethodMeta _TARGET;
    private final AnnotationMeta _ANNOTATION;
    private final InjectPosition _POSITION;

    private final boolean _HAS_PARAM_THIS;
    private final boolean _HAS_PARAM_CALLBACK_INFO;
    private final boolean _HAS_PARAM_CALLBACK_RETURNABLE;
    private final boolean _HAS_RETURN;

    public InjectMethodGenerator(AnnotatedMethod mixinMethod, InjectPosition targetPosition, Type targetType,
            AnnotationMeta mixinAnnotation) throws AnnotateException {
        _MIXIN = mixinMethod;
        _ANNOTATION = mixinAnnotation;
        _POSITION = targetPosition;

        String targetName = mixinAnnotation.getStringParam("targetName");

        if (targetName == null || targetName.isBlank()) {
            targetName = mixinMethod.getMethod().getName();
        }

        final AnnotatedMethodMeta mixinMeta = mixinMethod.getMeta();
        final Type[] mixinArguments = mixinMeta.getArgTypes();

        if (mixinArguments.length != 0) {
            final Type firstParam = mixinArguments[0];
            final Type lastParam = mixinArguments[mixinArguments.length - 1];

            _HAS_PARAM_THIS = firstParam.equals(targetType);
            _HAS_PARAM_CALLBACK_INFO = lastParam.equals(Type.getType(CallbackInfo.class));
            _HAS_PARAM_CALLBACK_RETURNABLE = lastParam.equals(Type.getType(CallbackInfoReturnable.class));
        } else {
            _HAS_PARAM_THIS = false;
            _HAS_PARAM_CALLBACK_INFO = false;
            _HAS_PARAM_CALLBACK_RETURNABLE = false;
        }

        final Type mixinReturnType = mixinMeta.getReturnType();
        _HAS_RETURN = mixinReturnType.equals(Type.VOID_TYPE);

        if (!mixinMeta.hasModifier(ACC_STATIC))
            throw new AnnotateException("Mixin method must be static.");

        if (!mixinMeta.hasModifier(ACC_PUBLIC))
            throw new AnnotateException("Mixin method must be public.");

        if (mixinMeta.hasModifier(ACC_ABSTRACT))
            throw new AnnotateException("Mixin method cannot be abstract.");

        if (_HAS_RETURN && _HAS_PARAM_CALLBACK_RETURNABLE)
            throw new AnnotateException(
                    "Mixin cannot have a non-void return type and a last parameter of CallbackInfoReturnable.");

        final AnnotatedTypeMeta targetMeta = AnnotatedTypeMeta.readMetadata(targetType.getClassName());
        final AnnotatedMethodMeta[] potentialTargets = targetMeta.getMethodsByName(targetName);
        AnnotatedMethodMeta target = null;

        if (potentialTargets.length == 0)
            throw new AnnotateException(
                    "No methods named '" + targetName + "' in '" + targetType.getClassName() + "'.");

        final int argIdxStart = _HAS_PARAM_THIS ? 1 : 0;
        final int argIdxEnd = mixinArguments.length
                - (_HAS_PARAM_CALLBACK_INFO | _HAS_PARAM_CALLBACK_RETURNABLE ? 1 : 0);
        final int argLength = argIdxEnd - argIdxStart;

        outer: for (AnnotatedMethodMeta potentialTarget : potentialTargets) {
            final Type[] arguments = potentialTarget.getArgTypes();
            final Type returnType = potentialTarget.getReturnType();

            if (_HAS_RETURN)
                if (!returnType.equals(mixinReturnType))
                    continue;

            if (arguments.length != argLength)
                continue;

            for (int i = 0; i < argLength; i++) {
                if (!arguments[i].equals(mixinArguments[argIdxStart + i]))
                    continue outer;
            }

            target = potentialTarget;
            break;
        }

        if (target == null)
            throw new AnnotateException("None of the methods named '" + targetName
                    + "' in target had the expected parameters and return type.");

        if (_HAS_PARAM_THIS && target.hasModifier(ACC_STATIC))
            throw new AnnotateException("Cannot get this of method '" + targetName
                    + "' as it is static.");

        Type targetReturnType = target.getReturnType();
        if (!targetReturnType.equals(Type.VOID_TYPE) && (_HAS_PARAM_CALLBACK_RETURNABLE || _HAS_RETURN)) {
            throw new AnnotateException("Mixin target '" + targetName + "' has no return type.");
        }

        _TARGET = target;
    }

    @Override
    public DynamicMixinAnnotation generateAnnotation() {
        DynamicMixinAnnotation inject = new DynamicMixinAnnotation(Inject.class);

        inject.setArrayParam("method", _TARGET.getName() + _TARGET.getDescriptor());
        DynamicMixinAnnotation at = new DynamicMixinAnnotation(At.class);
        at.setParam("id", _POSITION.getValue());
        inject.setAnnotationArrayParam("at", at);
        return inject;
    }

    @Override
    public String generateDescriptor() {
        Type[] targetArgs = _TARGET.getArgTypes();
        Type[] args = new Type[targetArgs.length + 1];
        for (int i = 0; i < targetArgs.length; i++)
            args[i] = targetArgs[i];
        Type targetReturnType = _TARGET.getReturnType();
        if (targetReturnType.equals(Type.VOID_TYPE))
            args[targetArgs.length] = Type.getType(CallbackInfo.class);
        else
            args[targetArgs.length] = Type.getType(CallbackInfoReturnable.class);
        return Type.getMethodDescriptor(Type.VOID_TYPE, args);
    }

    @Override
    public void generateMethod(MethodVisitor mw) {
        // pushes the 'out' field (of type PrintStream) of the System class
        mw.visitFieldInsn(GETSTATIC, "sh/emberj/annotate/core/Annotate", "LOG", "Lorg/slf4j/Logger;");
        // pushes the "Hello World!" String constant
        mw.visitLdcInsn("OMG WE DID IT HYPE!");
        // invokes the 'println' method (defined in the PrintStream class)
        mw.visitMethodInsn(INVOKEINTERFACE, "org/slf4j/Logger", "info", "(Ljava/lang/String;)V", true);
        mw.visitInsn(RETURN);
    }

    @Override
    public String getNamePrefix() {
        return "inject";
    }

}
