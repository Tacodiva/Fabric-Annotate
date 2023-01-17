package sh.emberj.annotate.core;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.spongepowered.asm.service.MixinService;

import net.fabricmc.loader.impl.launch.FabricLauncherBase;

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

    public static void injectClasspathFile(File file) throws AnnotateException {
        try {
            injectClasspathURL(file.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new AnnotateException("Error converting file to URL.", e);
        }
    }

    private static Method _addUrlMethod;

    public static void injectClasspathURL(URL url) throws AnnotateException {
        try {
            // This is actually an instance of KnotClassLoader.DynamicURLClassLoader
            if (_addUrlMethod == null) {
                Class<?> dynamicURLClassLoader = Class
                        .forName("net.fabricmc.loader.impl.launch.knot.KnotClassLoader$DynamicURLClassLoader");
                _addUrlMethod = dynamicURLClassLoader.getDeclaredMethod("addURL", new Class[] { URL.class });
                _addUrlMethod.setAccessible(true);
            }
            _addUrlMethod.invoke(FabricLauncherBase.getLauncher().getTargetClassLoader().getParent(),
                    new Object[] { url });
            Annotate.LOG.info("Injected URL '" + url + "' into classpath");
        } catch (Exception e) {
            throw new AnnotateException("Error while injecting URL into classpath.", e);
        }
    }

    public static Object instantiate(Type type) throws AnnotateException {
        return instantiate(loadClass(type));
    }

    public static Object instantiate(Class<?> clazz) throws AnnotateException {
        try {
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new AnnotateException(
                    "Failed to create instance. Could not find empty constructor. Make sure " + clazz
                            + " has a public constructor that takes no parameters.",
                    null, Type.getType(clazz), null, null);
        } catch (InvocationTargetException e) {
            throw new AnnotateException(
                    "Exception encountered from blank constructor of " + clazz + ".", null, Type.getType(clazz), null,
                    e.getCause());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
            throw new AnnotateException(
                    "Exception encountered while creating instance of " + clazz + ".", null, Type.getType(clazz), null,
                    e);
        }
    }

    public static String descriptorFromClassName(String className) {
        return "L" + className.replace('.', '/') + ";";
    }
}
