package sh.emberj.annotate.alloy.types;

import java.lang.reflect.Modifier;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import sh.emberj.annotate.alloy.AlloyArgument;
import sh.emberj.annotate.alloy.IAlloyMethod;
import sh.emberj.annotate.alloy.IAlloyMethodType;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.Utils;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.MethodMetadata;
import sh.emberj.annotate.core.asm.MutableAnnotationArrayMetadata;
import sh.emberj.annotate.core.asm.MutableAnnotationMetadata;
import sh.emberj.annotate.mixin.IDynamicMixinMethodGenerator;

public class AlloyInjectMethodType implements IAlloyMethodType {

    public final String POSITION;

    public AlloyInjectMethodType(String position) {
        POSITION = position;
    }

    @Override
    public IDynamicMixinMethodGenerator createDynamicMixin(AnnotationMetadata alloyAnnotation, AnnotatedMethod alloy,
            AlloyArgument[] alloyArgs, MethodMetadata target) throws AnnotateException {
        return new AlloyInjectMethod(alloyAnnotation, alloy, alloyArgs, target);
    }

    protected AnnotationMetadata generateAnnotation(AlloyInjectMethod method) {
        MutableAnnotationMetadata inject = new MutableAnnotationMetadata(Inject.class);
        MutableAnnotationArrayMetadata atArr = new MutableAnnotationArrayMetadata();
        MutableAnnotationMetadata at = new MutableAnnotationMetadata(At.class);
        at.setStringParam("value", POSITION);
        atArr.addAnnotation(at);
        inject.setArrayParam("at", atArr);
        MutableAnnotationArrayMetadata methodArr = new MutableAnnotationArrayMetadata();
        methodArr.addString(method.TARGET.getName() + method.TARGET.getDescriptor());
        inject.setArrayParam("method", methodArr);
        Boolean cancellable = method.ALLOY_ANNOTATION.getBooleanParam("cancellable");
        if (cancellable == null) {
            if (method.HAS_RETURN) {
                cancellable = true;
            } else {
                for (int i = 0; i < method.METHOD_ARGS.length; i++) {
                    AlloyArgument arg = method.METHOD_ARGS[i];
                    if (arg.alloyType() == null)
                        continue;
                    if (arg.alloyType().requireCancellable(arg)) {
                        cancellable = true;
                        break;
                    }
                }
                if (cancellable == null)
                    cancellable = false;
            }
        }
        inject.setBooleanParam("cancellable", cancellable);
        inject.setEnumParam("locals", LocalCapture.PRINT);
        return inject;
    }

    public class AlloyInjectMethod implements IAlloyMethod {

        public final AnnotationMetadata ALLOY_ANNOTATION;

        public final MethodMetadata METHOD;
        public final AnnotatedMethod ANNOTATED_METHOD;
        public final AlloyArgument[] METHOD_ARGS;

        public final MethodMetadata TARGET;

        public final boolean HAS_RETURN;
        private final int _CALLBACK_PARAM_INDEX;

        public AlloyInjectMethod(AnnotationMetadata alloyAnnotation, AnnotatedMethod method, AlloyArgument[] methodArgs,
                MethodMetadata target) throws AnnotateException {
            ANNOTATED_METHOD = method;
            METHOD = method.getMetadata();
            METHOD_ARGS = methodArgs;
            TARGET = target;
            ALLOY_ANNOTATION = alloyAnnotation;
            HAS_RETURN = !METHOD.getReturnType().equals(Type.VOID_TYPE);

            int targetArgsTotalSize = TARGET.hasModifier(Modifier.STATIC) ? 0 : 1;
            for (int i = 0; i < METHOD_ARGS.length; i++) {
                AlloyArgument arg = METHOD_ARGS[i];
                if (arg.alloyType() == null) {
                    int size = Utils.getVariableSize(arg.type());
                    targetArgsTotalSize += size;
                }
            }
            _CALLBACK_PARAM_INDEX = targetArgsTotalSize;

            if (!METHOD.hasModifier(Modifier.PUBLIC))
                throw new AnnotateException("Alloy methods must be public.", method);
            if (!METHOD.hasModifier(Modifier.STATIC))
                throw new AnnotateException("Alloy methods must be static.", method);
        }

        @Override
        public AnnotationMetadata generateAnnotation() {
            return AlloyInjectMethodType.this.generateAnnotation(this);
        }

        @Override
        public String generateDescriptor() {
            Type[] targetArgs = TARGET.getArgTypes();
            Type[] args = new Type[targetArgs.length + 1];
            for (int i = 0; i < targetArgs.length; i++)
                args[i] = targetArgs[i];
            Type targetReturnType = TARGET.getReturnType();
            if (targetReturnType.equals(Type.VOID_TYPE))
                args[targetArgs.length] = Type.getType(CallbackInfo.class);
            else
                args[targetArgs.length] = Type.getType(CallbackInfoReturnable.class);
            return Type.getMethodDescriptor(Type.VOID_TYPE, args);
        }

        @Override
        public int generateMethodFlags() {
            int flags = ACC_PRIVATE;
            if (TARGET.hasModifier(Modifier.STATIC))
                flags |= ACC_STATIC;
            return flags;
        }

        @Override
        public void generateMethod(MethodVisitor mw) throws AnnotateException {
            int mixinArgVarIdx = TARGET.hasModifier(Modifier.STATIC) ? 0 : 1;

            if (HAS_RETURN) {
                mw.visitVarInsn(ALOAD, _CALLBACK_PARAM_INDEX);
            }

            for (int i = 0; i < METHOD_ARGS.length; i++) {
                AlloyArgument arg = METHOD_ARGS[i];

                if (arg.alloyType() == null) {
                    mw.visitVarInsn(Utils.getVariableLoadOpcode(arg.type()), mixinArgVarIdx);
                    mixinArgVarIdx += Utils.getVariableSize(arg.type());
                } else {
                    try {
                        arg.alloyType().generateASM(mw, arg, this);
                    } catch (AnnotateException e) {
                        e.trySet(ANNOTATED_METHOD);
                        throw e;
                    }
                }
            }

            mw.visitMethodInsn(INVOKESTATIC, METHOD.getDeclaringClass().getType().getInternalName(), METHOD.getName(),
                    METHOD.getDescriptor(), false);

            if (HAS_RETURN) {
                Utils.convertToObject(mw, METHOD.getReturnType());
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
        public Type getTargetType() {
            return TARGET.getDeclaringClass().getType();
        }

        @Override
        public MethodMetadata getTarget() {
            return TARGET;
        }

        @Override
        public MethodMetadata getMethod() {
            return METHOD;
        }

        @Override
        public AlloyArgument[] getMethodArgs() {
            return METHOD_ARGS;
        }

        public int getCallbackParamIndex() {
            return _CALLBACK_PARAM_INDEX;
        }

        public String getPosition() {
            return POSITION;
        }
    }
}
