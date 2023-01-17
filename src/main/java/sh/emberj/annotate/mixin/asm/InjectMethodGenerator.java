package sh.emberj.annotate.mixin.asm;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.Utils;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadata;
import sh.emberj.annotate.core.asm.ClassMetadataFactory;
import sh.emberj.annotate.core.asm.MethodMetadata;
import sh.emberj.annotate.core.tiny.TinyMappedClass;
import sh.emberj.annotate.core.tiny.TinyMappedMethod;
import sh.emberj.annotate.core.tiny.TinyMapper;
import sh.emberj.annotate.core.tiny.TinyNamespace;

public class InjectMethodGenerator implements IDynamicMixinMethodGenerator {

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
    private final MethodMetadata _MIXIN_META;
    private final ClassMetadata _MIXIN_TYPE_META;
    private final MethodMetadata _TARGET_META;
    private final ClassMetadata _TARGET_TYPE_META;
    private final InjectPosition _POSITION;

    private final boolean _TARGET_IS_STATIC;
    private final boolean _HAS_PARAM_THIS;
    private final boolean _HAS_PARAM_RETURN_VAL;
    private final boolean _HAS_PARAM_CALLBACK_INFO;
    private final boolean _HAS_PARAM_CALLBACK_RETURNABLE;
    private final boolean _HAS_RETURN;

    public InjectMethodGenerator(AnnotatedMethod mixinMethod, InjectPosition targetPosition, Type targetType,
            AnnotationMetadata mixinAnnotation) throws AnnotateException {
        _MIXIN = mixinMethod;
        _MIXIN_META = _MIXIN.getMetadata();
        _MIXIN_TYPE_META = _MIXIN_META.getDeclaringClass();
        _TARGET_TYPE_META = ClassMetadataFactory.create(targetType);
        _POSITION = targetPosition;

        if (Utils.isClassLoaded(targetType.getClassName()))
            throw new AnnotateException(
                    "Mixin target class " + targetType.getClassName() + " has already been loaded!");

        String unmappedTargetName = mixinAnnotation.getStringParam("targetName");
        if (unmappedTargetName == null || unmappedTargetName.isBlank()) {
            unmappedTargetName = _MIXIN_META.getName();
        }
        final TinyMapper mapper = Annotate.getTinyMapper();
        final TinyNamespace namespace = mapper.getNamespace("named");

        final Set<MethodMetadata> potentialTargets = new HashSet<>();
        {
            final TinyMappedClass targetMapped = mapper.getClass(targetType, namespace);
            if (targetMapped != null) {
                for (TinyMappedMethod mappedMethod : targetMapped.getMethods(unmappedTargetName))
                    potentialTargets.add(mappedMethod.getMethodMetadata());
            } else {
                final ClassMetadata targetTypeMeta = ClassMetadataFactory.create(targetType);
                for (MethodMetadata potentialTarget : targetTypeMeta.getMethodsByName(unmappedTargetName))
                    potentialTargets.add(potentialTarget);
            }
        }

        final Type[] mixinArguments = _MIXIN_META.getArgTypes();
        Type mixinReturnType = _MIXIN_META.getReturnType();

        _HAS_RETURN = !mixinReturnType.equals(Type.VOID_TYPE);

        if (mixinArguments.length != 0) {
            final Type firstParam = mixinArguments[0];
            final Type lastParam = mixinArguments[mixinArguments.length - 1];

            _HAS_PARAM_THIS = firstParam.equals(targetType);
            _HAS_PARAM_CALLBACK_RETURNABLE = lastParam.equals(TYPE_CALLBACK_RETURNABLE);

            boolean hasCallbackInfo = lastParam.equals(TYPE_CALLBACK_INFO);
            boolean hasReturnArg = false;

            if (mixinArguments.length != 1) {
                final Type secondLastParam = mixinArguments[mixinArguments.length - 2];
                if (secondLastParam.equals(TYPE_CALLBACK_RETURNABLE))
                    throw new AnnotateException(
                            "Second last parameter must be CallbackInfo, not CallbackInfoReturnable.");
                if (secondLastParam.equals(TYPE_CALLBACK_INFO)) {
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

        if (!_MIXIN_META.hasModifier(ACC_STATIC))
            throw new AnnotateException("Mixin method must be static.");

        if (!_MIXIN_META.hasModifier(ACC_PUBLIC))
            throw new AnnotateException("Mixin method must be public.");

        if (_MIXIN_META.hasModifier(ACC_ABSTRACT))
            throw new AnnotateException("Mixin method cannot be abstract.");

        if (_HAS_RETURN && _HAS_PARAM_CALLBACK_RETURNABLE)
            throw new AnnotateException(
                    "Mixin cannot have a non-void return type and a CallbackInfoReturnable parameter.");

        MethodMetadata target = null;

        if (potentialTargets.size() == 0)
            throw new AnnotateException(
                    "No methods named '" + unmappedTargetName + "' in '" + targetType.getClassName() + "'.");

        final int argIdxStart = _HAS_PARAM_THIS ? 1 : 0;
        final int argIdxEnd = mixinArguments.length
                - (_HAS_PARAM_CALLBACK_INFO | _HAS_PARAM_CALLBACK_RETURNABLE ? 1 : 0)
                - (_HAS_PARAM_RETURN_VAL ? 1 : 0);
        final int argLength = argIdxEnd - argIdxStart;

        outer: for (MethodMetadata potentialTarget : potentialTargets) {
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
                    "None of the methods named '" + unmappedTargetName + "' in target had the expected parameters.");

        if ((_HAS_RETURN || _HAS_PARAM_RETURN_VAL) && !mixinReturnType.equals(target.getReturnType()))
            throw new AnnotateException(
                    "Target method '" + unmappedTargetName + "' had the incorrect return type '"
                            + target.getReturnType().getClassName() + "''. (Expected '" + mixinReturnType.getClassName()
                            + "')");

        _TARGET_IS_STATIC = target.hasModifier(ACC_STATIC);

        if (_HAS_PARAM_THIS && _TARGET_IS_STATIC)
            throw new AnnotateException("Cannot get this of method '" + unmappedTargetName
                    + "' as it is static.");

        Type targetReturnType = target.getReturnType();
        if (targetReturnType.equals(Type.VOID_TYPE) && (_HAS_PARAM_CALLBACK_RETURNABLE || _HAS_RETURN)) {
            throw new AnnotateException("Mixin target '" + unmappedTargetName + "' has no return type.");
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
            mw.visitMethodInsn(INVOKEVIRTUAL, TYPE_CALLBACK_RETURNABLE.getInternalName(), "getReturnValue",
                    "()Ljava/lang/Object;", false);
            if (Utils.isPrimitive(_TARGET_META.getReturnType()))
                Utils.convertFromObject(mw, _TARGET_META.getReturnType());
        }

        mw.visitMethodInsn(INVOKESTATIC, _MIXIN_TYPE_META.getType().getInternalName(), _MIXIN_META.getName(),
                _MIXIN_META.getDescriptor(), false);

        if (_HAS_RETURN) {
            Utils.convertToObject(mw, _MIXIN_META.getReturnType());
            mw.visitMethodInsn(INVOKEVIRTUAL, TYPE_CALLBACK_RETURNABLE.getInternalName(), "setReturnValue",
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

    @Override
    public Type getTargetType() {
        return _TARGET_TYPE_META.getType();
    }

}
