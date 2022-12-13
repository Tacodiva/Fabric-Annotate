package sh.emberj.annotate.mixin.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.Utils;
import sh.emberj.annotate.core.asm.AnnotatedMethodMeta;
import sh.emberj.annotate.core.asm.AnnotatedTypeMeta;
import sh.emberj.annotate.core.asm.AnnotationMeta;

public class InjectMethodGenerator implements IDynamicMixinMethodGenerator {

    private static final Type _TYPE_CALLBACK_INFO = Type.getType(CallbackInfo.class);
    private static final Type _TYPE_CALLBACK_RETURNABLE = Type.getType(CallbackInfoReturnable.class);

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
    private final AnnotatedMethodMeta _MIXIN_META;
    private final AnnotatedTypeMeta _MIXIN_TYPE_META;
    private final AnnotatedMethodMeta _TARGET_META;
    private final InjectPosition _POSITION;

    private final boolean _TARGET_IS_STATIC;
    private final boolean _HAS_PARAM_THIS;
    private final boolean _HAS_PARAM_RETURN_VAL;
    private final boolean _HAS_PARAM_CALLBACK_INFO;
    private final boolean _HAS_PARAM_CALLBACK_RETURNABLE;
    private final boolean _HAS_RETURN;

    public InjectMethodGenerator(AnnotatedMethod mixinMethod, InjectPosition targetPosition, Type targetType,
            AnnotationMeta mixinAnnotation) throws AnnotateException {
        _MIXIN = mixinMethod;
        _MIXIN_META = _MIXIN.getMeta();
        _MIXIN_TYPE_META = AnnotatedTypeMeta.readMetadata(_MIXIN.getDeclaringClass());
        _POSITION = targetPosition;

        if (Utils.isClassLoaded(targetType.getClassName()))
            throw new AnnotateException(
                    "Mixin target class " + targetType.getClassName() + " has already been loaded!");

        String targetName = mixinAnnotation.getStringParam("targetName");

        if (targetName == null || targetName.isBlank()) {
            targetName = mixinMethod.getName();
        }

        final AnnotatedMethodMeta mixinMeta = mixinMethod.getMeta();
        final Type[] mixinArguments = mixinMeta.getArgTypes();
        Type mixinReturnType = mixinMeta.getReturnType();

        _HAS_RETURN = !mixinReturnType.equals(Type.VOID_TYPE);

        if (mixinArguments.length != 0) {
            final Type firstParam = mixinArguments[0];
            final Type lastParam = mixinArguments[mixinArguments.length - 1];

            _HAS_PARAM_THIS = firstParam.equals(targetType);
            _HAS_PARAM_CALLBACK_RETURNABLE = lastParam.equals(_TYPE_CALLBACK_RETURNABLE);

            boolean hasCallbackInfo = lastParam.equals(_TYPE_CALLBACK_INFO);
            boolean hasReturnArg = false;

            if (mixinArguments.length != 1) {
                final Type secondLastParam = mixinArguments[mixinArguments.length - 2];
                if (secondLastParam.equals(_TYPE_CALLBACK_RETURNABLE))
                    throw new AnnotateException(
                            "Second last parameter must be CallbackInfo, not CallbackInfoReturnable.");
                if (secondLastParam.equals(_TYPE_CALLBACK_INFO)) {
                    if (hasCallbackInfo || _HAS_PARAM_CALLBACK_RETURNABLE)
                        throw new AnnotateException("Duplicate CallbackInfo argument.");
                    if (_HAS_RETURN && !lastParam.equals(mixinReturnType))
                        throw new AnnotateException("Expected last parameter to match return type.");
                    mixinReturnType = lastParam;
                    hasCallbackInfo = true;
                    hasReturnArg = true;
                }
            }
            _HAS_PARAM_CALLBACK_INFO = hasCallbackInfo;
            _HAS_PARAM_RETURN_VAL = hasReturnArg;
        } else {
            _HAS_PARAM_THIS = false;
            _HAS_PARAM_RETURN_VAL = false;
            _HAS_PARAM_CALLBACK_INFO = false;
            _HAS_PARAM_CALLBACK_RETURNABLE = false;
        }

        if (!mixinMeta.hasModifier(ACC_STATIC))
            throw new AnnotateException("Mixin method must be static.");

        if (!mixinMeta.hasModifier(ACC_PUBLIC))
            throw new AnnotateException("Mixin method must be public.");

        if (mixinMeta.hasModifier(ACC_ABSTRACT))
            throw new AnnotateException("Mixin method cannot be abstract.");

        if (_HAS_RETURN && _HAS_PARAM_CALLBACK_RETURNABLE)
            throw new AnnotateException(
                    "Mixin cannot have a non-void return type and a CallbackInfoReturnable parameter.");

        final AnnotatedTypeMeta targetMeta = AnnotatedTypeMeta.readMetadata(targetType);
        final AnnotatedMethodMeta[] potentialTargets = targetMeta.getMethodsByName(targetName);
        AnnotatedMethodMeta target = null;

        if (potentialTargets.length == 0)
            throw new AnnotateException(
                    "No methods named '" + targetName + "' in '" + targetType.getClassName() + "'.");

        final int argIdxStart = _HAS_PARAM_THIS ? 1 : 0;
        final int argIdxEnd = mixinArguments.length
                - (_HAS_PARAM_CALLBACK_INFO | _HAS_PARAM_CALLBACK_RETURNABLE ? 1 : 0)
                - (_HAS_PARAM_RETURN_VAL ? 1 : 0);
        final int argLength = argIdxEnd - argIdxStart;

        outer: for (AnnotatedMethodMeta potentialTarget : potentialTargets) {
            final Type[] arguments = potentialTarget.getArgTypes();

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
            throw new AnnotateException(
                    "None of the methods named '" + targetName + "' in target had the expected parameters.");

        if ((_HAS_RETURN || _HAS_PARAM_RETURN_VAL) && !mixinReturnType.equals(target.getReturnType()))
            throw new AnnotateException(
                    "Target method '" + targetName + "' had the incorrect return type '"
                            + target.getReturnType().getClassName() + "''. (Expected '" + mixinReturnType.getClassName()
                            + "')");

        _TARGET_IS_STATIC = target.hasModifier(ACC_STATIC);

        if (_HAS_PARAM_THIS && _TARGET_IS_STATIC)
            throw new AnnotateException("Cannot get this of method '" + targetName
                    + "' as it is static.");

        Type targetReturnType = target.getReturnType();
        if (targetReturnType.equals(Type.VOID_TYPE) && (_HAS_PARAM_CALLBACK_RETURNABLE || _HAS_RETURN)) {
            throw new AnnotateException("Mixin target '" + targetName + "' has no return type.");
        }

        _TARGET_META = target;
    }

    @Override
    public DynamicMixinAnnotation generateAnnotation() {
        DynamicMixinAnnotation inject = new DynamicMixinAnnotation(Inject.class, true);

        DynamicMixinAnnotation at = new DynamicMixinAnnotation(At.class, true);
        at.setParam("value", _POSITION.getValue());
        inject.setAnnotationArrayParam("at", at);
        inject.setArrayParam("method", _TARGET_META.getName() + _TARGET_META.getDescriptor());
        inject.setParam("cancellable", _HAS_RETURN || _HAS_PARAM_CALLBACK_INFO || _HAS_PARAM_CALLBACK_RETURNABLE);
        return inject;
    }

    @Override
    public String generateDescriptor() {
        Type[] targetArgs = _TARGET_META.getArgTypes();
        Type[] args = new Type[targetArgs.length + 1];
        for (int i = 0; i < targetArgs.length; i++)
            args[i] = targetArgs[i];
        Type targetReturnType = _TARGET_META.getReturnType();
        if (targetReturnType.equals(Type.VOID_TYPE))
            args[targetArgs.length] = Type.getType(CallbackInfo.class);
        else
            args[targetArgs.length] = Type.getType(CallbackInfoReturnable.class);
        return Type.getMethodDescriptor(Type.VOID_TYPE, args);
    }

    @Override
    public void generateMethod(MethodVisitor mw) {

        final Type[] targetArgs = _TARGET_META.getArgTypes();
        final int[] targetArgSizes = new int[targetArgs.length];

        int targetArgsTotalSize = 0;
        for (int i = 0; i < targetArgs.length; i++) {
            int size = Utils.getVariableSize(targetArgs[i]);
            targetArgSizes[i] = size;
            targetArgsTotalSize += size;
        }

        final int argVarIdx = _TARGET_IS_STATIC ? 0 : 1;
        final int argInfoIdx = targetArgsTotalSize + argVarIdx;

        if (_HAS_RETURN) {
            mw.visitVarInsn(ALOAD, argInfoIdx);
        }

        if (_HAS_PARAM_THIS) {
            mw.visitVarInsn(ALOAD, 0);
        }

        {
            int varIdx = argVarIdx;
            for (Type argument : targetArgs) {
                mw.visitVarInsn(Utils.getVariableLoadOpcode(argument), varIdx);
                varIdx += Utils.getVariableSize(argument);
            }
        }

        if (_HAS_PARAM_CALLBACK_INFO || _HAS_PARAM_CALLBACK_RETURNABLE) {
            mw.visitVarInsn(ALOAD, argInfoIdx);
        }

        if (_HAS_PARAM_RETURN_VAL) {
            mw.visitVarInsn(ALOAD, argInfoIdx);
            mw.visitMethodInsn(INVOKEVIRTUAL, _TYPE_CALLBACK_RETURNABLE.getInternalName(), "getReturnValue",
                    "()Ljava/lang/Object;", false);
            if (Utils.isPrimitive(_TARGET_META.getReturnType()))
                Utils.convertFromObject(mw, _TARGET_META.getReturnType());
        }

        mw.visitMethodInsn(INVOKESTATIC, _MIXIN_TYPE_META.getType().getInternalName(), _MIXIN.getName(),
                _MIXIN_META.getDescriptor(), false);

        if (_HAS_RETURN) {
            Utils.convertToObject(mw, _MIXIN_META.getReturnType());
            mw.visitMethodInsn(INVOKEVIRTUAL, _TYPE_CALLBACK_RETURNABLE.getInternalName(), "setReturnValue",
                    "(Ljava/lang/Object;)V", false);
        }

        mw.visitInsn(RETURN);
    }

    @Override
    public String getNamePrefix() {
        return "inject";
    }

    @Override
    public int generateMethodFlags() {
        int flags = ACC_PRIVATE;
        if (_TARGET_IS_STATIC)
            flags |= ACC_STATIC;
        return flags;
    }

}
