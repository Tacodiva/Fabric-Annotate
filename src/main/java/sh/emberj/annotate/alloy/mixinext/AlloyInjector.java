package sh.emberj.annotate.alloy.mixinext;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes.InjectionNode;
import org.spongepowered.asm.mixin.injection.struct.Target;

import sh.emberj.annotate.alloy.AlloyMethodArg;
import sh.emberj.annotate.alloy.AlloyMethodArgType;
import sh.emberj.annotate.alloy.IAlloyInjection;
import sh.emberj.annotate.alloy.Return;
import sh.emberj.annotate.alloy.ReturnVoid;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.Utils;
import sh.emberj.annotate.core.Utils.PrimitiveTypeInfo;
import sh.emberj.annotate.core.asm.MethodMetadata;

public class AlloyInjector extends Injector implements Opcodes {

    public static final Type TYPE_RETURN = Type.getType(Return.class);
    public static final Type TYPE_RETURN_VOID = Type.getType(ReturnVoid.class);

    private static final List<IAlloyInjection> _ID_MAP = new ArrayList<>();

    public static IAlloyInjection getMethod(int id) {
        return _ID_MAP.get(id);
    }

    public static int registerMethod(IAlloyInjection method) {
        _ID_MAP.add(method);
        return _ID_MAP.size() - 1;
    }

    private final IAlloyInjection _injection;
    private final MethodMetadata _alloyMethod;

    private int _returnLVI;
    private boolean _allocateReturnLVI;
    private Target _currentTarget;
    private InjectionNode _currentTargetNode;

    public AlloyInjector(AlloyInjectInfo info, int id) {
        super(info, "@AlloyInject");
        _injection = getMethod(id);
        _alloyMethod = _injection.getAlloyMethod().getMetadata();
        _returnLVI = -1;
        _allocateReturnLVI = false;
    }

    @Override
    protected void preInject(Target target, InjectionNode node) {
        _currentTarget = target;
        _currentTargetNode = node;

        try {
            for (AlloyMethodArg alloyArg : _injection.getAlloyArgs()) {
                AlloyMethodArgType type = alloyArg.alloyType();
                if (type != null)
                    type.preInject(this, alloyArg, _injection);
            }

        } catch (AnnotateException e) {
            handle(e);
        }
    }

    @Override
    protected void inject(Target target, InjectionNode node) {
        _currentTarget = target;
        _currentTargetNode = node;
        try {
            InsnList injectInsn = new InsnList();

            if (_allocateReturnLVI) {
                _returnLVI = target.allocateLocals(Utils.getVariableSize(target.returnType));
                injectInsn.add(new InsnNode(Utils.getDUPOpcode(target.returnType)));
                injectInsn.add(new VarInsnNode(target.returnType.getOpcode(ISTORE), _returnLVI));
            }

            int argVarIndex = target.isStatic ? 0 : 1;
            for (AlloyMethodArg arg : _injection.getAlloyArgs()) {

                if (arg.alloyType() == null) {
                    injectInsn.add(new VarInsnNode(arg.type().getOpcode(ILOAD), argVarIndex));
                    argVarIndex += Utils.getVariableSize(arg.type());
                } else {
                    arg.alloyType().inject(injectInsn, this, arg, _injection);
                }
            }

            injectInsn.add(new MethodInsnNode(INVOKESTATIC,
                    _alloyMethod.getDeclaringClass().getType().getInternalName(),
                    _alloyMethod.getName(),
                    _alloyMethod.getDescriptor(), false));
            info.addCallbackInvocation(null);

            Type alloyMethodReturn = _alloyMethod.getReturnType();
            if (!alloyMethodReturn.equals(Type.VOID_TYPE)) {
                if (alloyMethodReturn.equals(TYPE_RETURN_VOID)) {
                    if (!target.returnType.equals(Type.VOID_TYPE))
                        throw new AnnotateException(
                                "Can only return ReturnVoid on methods whos targets return void. Use Return instead.");
                    LabelNode resumeLabel = new LabelNode();
                    // if (returnVal != null) {
                    injectInsn.add(new JumpInsnNode(IFNULL, resumeLabel));
                    // return
                    injectInsn.add(new InsnNode(RETURN));
                    // }
                    injectInsn.add(resumeLabel);
                } else if (alloyMethodReturn.equals(TYPE_RETURN)) {
                    if (target.returnType.equals(Type.VOID_TYPE))
                        throw new AnnotateException(
                                "Can only return Return on methods whos targets do not return void. Use ReturnVoid instead.");
                    // if (returnVal != null) {
                    LabelNode resumeLabel = new LabelNode();
                    injectInsn.add(new InsnNode(DUP));
                    injectInsn.add(new JumpInsnNode(IFNULL, resumeLabel));
                    // returnVal = returnVal.value
                    injectInsn.add(
                            new FieldInsnNode(GETFIELD, TYPE_RETURN.getInternalName(), "value", "Ljava/lang/Object;"));
                    // cast object to whatever the return value is
                    PrimitiveTypeInfo targetReturnPrimInfo = Utils.getPrimitiveInfo(target.returnType);
                    if (targetReturnPrimInfo != null) {
                        injectInsn.add(
                                new TypeInsnNode(CHECKCAST, targetReturnPrimInfo.objectType().getInternalName()));
                        injectInsn.add(new MethodInsnNode(INVOKEVIRTUAL,
                                targetReturnPrimInfo.objectType().getInternalName(), targetReturnPrimInfo.valueMethod(),
                                Type.getMethodDescriptor(targetReturnPrimInfo.type()), false));
                    } else {
                        if (!target.returnType.getDescriptor().equals("Ljava/lang/Object;"))
                            injectInsn.add(
                                    new TypeInsnNode(CHECKCAST, target.returnType.getInternalName()));
                    }
                    // return returnVal
                    injectInsn.add(new InsnNode(target.returnType.getOpcode(IRETURN)));
                    // }
                    injectInsn.add(resumeLabel);
                } else {
                    if (!target.returnType.equals(alloyMethodReturn))
                        throw new AnnotateException("Unexpected method return " + alloyMethodReturn + ". Expected '"
                                + target.returnType + "', Return or ReturnVoid.");
                    injectInsn.add(new InsnNode(target.returnType.getOpcode(IRETURN)));
                }
                injectInsn.add(new InsnNode(Utils.getPOPOpcode(target.returnType)));
            }

            target.insertBefore(node, injectInsn);

            // for (AbstractInsnNode idk : target) {
            //     System.out.println(Utils.insnToString(idk));
            // }
        } catch (AnnotateException e) {
            handle(e);
        }
    }

    private void handle(AnnotateException e) {
        e.trySet(_injection.getAlloyMethod());
        e.showGUI();
        throw new RuntimeException(e);
    }

    public int getReturnLVI() {
        if (_returnLVI < 0)
            throw new IllegalStateException("No LVI allocated for the return value yet.");
        return _returnLVI;
    }

    public void allocateReturnLVI() {
        _allocateReturnLVI = true;
    }

    public Target getCurrentTarget() {
        return _currentTarget;
    }

    public InjectionNode getCurrentTargetNode() {
        return _currentTargetNode;
    }

    public InjectionInfo getInfo() {
        return info;
    }
}