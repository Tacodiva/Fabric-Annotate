package sh.emberj.annotate.alloy;

import java.lang.reflect.Modifier;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.injection.At;

import sh.emberj.annotate.alloy.mixinext.AlloyInject;
import sh.emberj.annotate.alloy.mixinext.AlloyInjector;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.MethodMetadata;
import sh.emberj.annotate.core.asm.MutableAnnotationArrayMetadata;
import sh.emberj.annotate.core.asm.MutableAnnotationMetadata;

public class AlloyInjectionType implements IAlloyInjectionType {
    public final String position;

    public AlloyInjectionType(String position) {
        this.position = position;
    }

    @Override
    public IAlloyInjection createInjection(AnnotationMetadata alloyAnnotation, AnnotatedMethod alloy,
            AlloyMethodArg[] alloyArgs, MethodMetadata target) throws AnnotateException {
        return new AlloyInjection(alloyAnnotation, alloy, alloyArgs, target);
    }

    public class AlloyInjection implements IAlloyInjection {
        public final AnnotationMetadata annotation;
        public final int id;

        public final AnnotatedMethod alloyMethod;
        public final AlloyMethodArg[] alloyArgs;

        public final MethodMetadata targetMethod;

        public AlloyInjection(AnnotationMetadata alloyAnnotation, AnnotatedMethod method, AlloyMethodArg[] methodArgs,
                MethodMetadata target) throws AnnotateException {
            alloyMethod = method;
            targetMethod = target;
            annotation = alloyAnnotation;
            alloyArgs = methodArgs;
            id = AlloyInjector.registerMethod(this);

            if (!alloyMethod.getMetadata().hasModifier(Modifier.PUBLIC))
                throw new AnnotateException("Alloy methods must be public.", method);
            if (!alloyMethod.getMetadata().hasModifier(Modifier.STATIC))
                throw new AnnotateException("Alloy methods must be static.", method);
        }

        @Override
        public AnnotationMetadata generateAnnotation() {
            MutableAnnotationMetadata inject = new MutableAnnotationMetadata(AlloyInject.class);

            MutableAnnotationArrayMetadata atArr = new MutableAnnotationArrayMetadata();
            MutableAnnotationMetadata at = new MutableAnnotationMetadata(At.class);
            at.setStringParam("value", position);
            atArr.addAnnotation(at);
            inject.setArrayParam("at", atArr);

            MutableAnnotationArrayMetadata methodArr = new MutableAnnotationArrayMetadata();
            methodArr.addString(targetMethod.getName() + targetMethod.getDescriptor());
            inject.setArrayParam("method", methodArr);

            inject.setIntParam("annotateID", id);

            return inject;
        }

        @Override
        public String generateDescriptor() {
            return Type.getMethodDescriptor(Type.VOID_TYPE);
        }

        @Override
        public int generateMethodFlags() {
            int flags = ACC_PRIVATE;
            if (targetMethod.hasModifier(Modifier.STATIC))
                flags |= ACC_STATIC;
            return flags;
        }

        @Override
        public void generateMethod(MethodVisitor mw) throws AnnotateException {
            mw.visitInsn(RETURN);
        }

        @Override
        public String getNamePrefix() {
            return "inject";
        }

        @Override
        public Type getTargetType() {
            return targetMethod.getDeclaringClass().getType();
        }

        @Override
        public MethodMetadata getTarget() {
            return targetMethod;
        }

        @Override
        public AlloyMethodArg[] getAlloyArgs() {
            return alloyArgs;
        }

        @Override
        public AnnotatedMethod getAlloyMethod() {
            return alloyMethod;
        }

        public String getPosition() {
            return position;
        }
    }
}
