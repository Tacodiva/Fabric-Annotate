package sh.emberj.annotate.alloy;

import java.util.Collection;

import org.objectweb.asm.Type;

import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.asm.AnnotationContainer;
import sh.emberj.annotate.core.asm.AnnotationMetadata;
import sh.emberj.annotate.core.asm.ClassMetadata;
import sh.emberj.annotate.core.asm.MethodMetadata;
import sh.emberj.annotate.core.tiny.TinyMappedClass;
import sh.emberj.annotate.core.tiny.TinyMappedMethod;
import sh.emberj.annotate.core.tiny.TinyMapper;
import sh.emberj.annotate.core.tiny.TinyNamespace;
import sh.emberj.annotate.mixin.AnnotateMixins;

public interface IAlloyInjectionType {

    public IAlloyInjection createInjection(AnnotationMetadata alloyAnnotation, AnnotatedMethod alloy,
            AlloyMethodArg[] alloyArgs, MethodMetadata target) throws AnnotateException;

    public static IAlloyInjection createFromMethod(IAlloyInjectionType type, AnnotationMetadata alloyAnnotation, AnnotatedMethod method,
            ClassMetadata targetClass,
            String targetName)
            throws AnnotateException {

        Type[] methodArgs = method.getMetadata().getArgTypes();

        AlloyMethodArg[] alloyArgs = new AlloyMethodArg[methodArgs.length];

        int foundArgs = 0;
        for (int i = 0; i < methodArgs.length; i++) {
            AnnotationContainer argAnnotations = method.getMetadata().getArgAnnotations(i);
            if (argAnnotations != null)
                for (AnnotationMetadata annotation : argAnnotations.getAnnotations()) {
                    AlloyMethodArgType argType = AlloyMethodArgTypeRegistry.INSTANCE.getArgument(annotation.getType());
                    if (argType == null)
                        continue;
                    alloyArgs[i] = new AlloyMethodArg(argType, annotation, methodArgs[i]);
                    ++foundArgs;
                    break;
                }
            if (alloyArgs[i] == null)
                alloyArgs[i] = new AlloyMethodArg(null, null, methodArgs[i]);
        }

        Type[] targetArgs = new Type[methodArgs.length - foundArgs];
        {
            int targetI = 0;
            for (int i = 0; i < methodArgs.length; i++) {
                if (alloyArgs[i].annotation() != null)
                    continue;
                targetArgs[targetI++] = methodArgs[i];
            }
        }

        if (targetName == null)
            targetName = method.getMetadata().getName();

        final TinyMapper mapper = Annotate.getTinyMapper();
        final TinyNamespace namespace = mapper.getNamespace("named");
        MethodMetadata[] potentialTargets;
        {
            final TinyMappedClass targetMapped = mapper.getClass(targetClass.getType(), namespace);
            if (targetMapped != null) {
                Collection<TinyMappedMethod> mappedMethods = targetMapped.getMethods(targetName);
                potentialTargets = new MethodMetadata[mappedMethods.size()];
                int i = 0;
                for (TinyMappedMethod mappedMethod : targetMapped.getMethods(targetName))
                    potentialTargets[i++] = mappedMethod.getMethodMetadata();
            } else {
                potentialTargets = targetClass.getMethodsByName(targetName);
            }
        }

        if (potentialTargets.length == 0)
            throw new AnnotateException("Cannot find any methods named '" + targetName + "' in " + targetClass + ".",
                    method);

        MethodMetadata target = null;
        targetLoop: for (MethodMetadata potentialTarget : potentialTargets) {
            Type[] potentialTargetArgs = potentialTarget.getArgTypes();
            if (targetArgs.length != potentialTargetArgs.length)
                continue;
            for (int i = 0; i < targetArgs.length; i++) {
                if (!targetArgs[i].equals(potentialTargetArgs[i]))
                    continue targetLoop;
            }
            target = potentialTarget;
            break;
        }

        if (target == null)
            throw new AnnotateException("Cannot find target method in target type " + targetClass + ". Expected "
                    + MethodMetadata.toString(targetName, targetArgs), method);

        IAlloyInjection injection = type.createInjection(alloyAnnotation, method, alloyArgs, target);
        AnnotateMixins.addMixin(injection);
        return injection;
    }
}
