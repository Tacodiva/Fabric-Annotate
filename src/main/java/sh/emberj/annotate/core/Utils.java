package sh.emberj.annotate.core;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.spongepowered.asm.service.MixinService;

public class Utils implements Opcodes {

    private Utils() {
    }

    private static record PrimitiveTypeInfo(Type type, Class<?> clazz, Type objectType, String valueMethod,
            int lvOpcode, int variableSize) {
    }

    private static final Map<String, PrimitiveTypeInfo> _PRIMITIVE_INFO = new HashMap<>() {
        private void addPrimitive(Type type, Class<?> clazz, Type objectType, String valueMethod, int lvOpcode,
                int variableSize) {
            put(type.getClassName(),
                    new PrimitiveTypeInfo(type, clazz, objectType, valueMethod, lvOpcode, variableSize));
        }

        {
            addPrimitive(Type.BYTE_TYPE, byte.class, Type.getType(Byte.class), "byteValue", ILOAD, 1);
            addPrimitive(Type.BOOLEAN_TYPE, boolean.class, Type.getType(Boolean.class), "booleanValue", ILOAD, 1);
            addPrimitive(Type.CHAR_TYPE, char.class, Type.getType(Character.class), "charValue", ILOAD, 1);
            addPrimitive(Type.SHORT_TYPE, short.class, Type.getType(Short.class), "shortValue", ILOAD, 1);
            addPrimitive(Type.INT_TYPE, int.class, Type.getType(Integer.class), "intValue", ILOAD, 1);
            addPrimitive(Type.FLOAT_TYPE, float.class, Type.getType(Float.class), "floatValue", FLOAD, 1);
            addPrimitive(Type.LONG_TYPE, long.class, Type.getType(Long.class), "longValue", LLOAD, 2);
            addPrimitive(Type.DOUBLE_TYPE, double.class, Type.getType(Double.class), "doubleValue", DLOAD, 2);
        }
    };

    private static PrimitiveTypeInfo getPrimitiveInfo(Type type) {
        return _PRIMITIVE_INFO.get(type.getClassName());
    }

    public static void convertToObject(MethodVisitor mw, Type type) {
        PrimitiveTypeInfo info = getPrimitiveInfo(type);
        if (info != null) {
            mw.visitMethodInsn(INVOKESTATIC, info.objectType.getInternalName(), "valueOf",
                    Type.getMethodDescriptor(info.objectType, info.type), false);
        }
    }

    public static void convertFromObject(MethodVisitor mw, Type type) {
        PrimitiveTypeInfo info = getPrimitiveInfo(type);
        if (info != null) {
            mw.visitTypeInsn(CHECKCAST, info.objectType.getInternalName());
            mw.visitMethodInsn(INVOKEVIRTUAL, info.objectType.getInternalName(), info.valueMethod,
                    Type.getMethodDescriptor(info.type), false);
        }
    }

    public static boolean isPrimitive(Type type) {
        return getPrimitiveInfo(type) != null;
    }

    public static int getVariableSize(Type type) {
        PrimitiveTypeInfo info = getPrimitiveInfo(type);
        if (info == null)
            return 1;
        return info.variableSize;
    }

    public static int getVariableLoadOpcode(Type type) {
        PrimitiveTypeInfo info = getPrimitiveInfo(type);
        if (info == null)
            return ALOAD;
        return info.lvOpcode;
    }

    public static boolean isClassLoaded(String className) {
        return MixinService.getService().getClassTracker().isClassLoaded(className);
    }

    public static Class<?> loadClass(Type type) {
        PrimitiveTypeInfo info = getPrimitiveInfo(type);
        if (info != null)
            return info.clazz;
        try {
            return Class.forName(type.getClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
