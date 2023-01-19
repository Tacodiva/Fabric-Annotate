package sh.emberj.annotate.core.asm;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.Utils;

public class ClassMetadataFactory {

    private static Map<String, ClassMetadata> _CAHCE = new HashMap<>();
    private static ImmutableSet<ClassInfo> _allClasses;

    static {
    }

    private static ImmutableSet<ClassInfo> getAllClasses() throws AnnotateException {
        if (_allClasses != null)
            return _allClasses;
        long start = System.currentTimeMillis();
        try {
            _allClasses = ClassPath.from(ClassMetadataFactory.class.getClassLoader()).getAllClasses();
        } catch (IOException e) {
            throw new AnnotateException("Error while enumerating classpath.", e);
        }
        Annotate.LOG
                .info("Found " + _allClasses.size() + " classes in " + (System.currentTimeMillis() - start) + "ms");
        return _allClasses;
    }

    private static ImmutableSet<ClassInfo> getAllClassesRecursive(String packageName) throws AnnotateException {
        String packagePrefix = packageName + '.';
        ImmutableSet.Builder<ClassInfo> builder = ImmutableSet.builder();
        for (ClassInfo classInfo : getAllClasses()) {
            if (classInfo.getName().startsWith(packagePrefix)) {
                builder.add(classInfo);
            }
        }
        return builder.build();
    }

    public static Set<ClassMetadata> createAll(String package_) throws AnnotateException {
        Set<ClassMetadata> classes = new HashSet<>();
        ImmutableSet<ClassInfo> packageContent = getAllClassesRecursive(package_);

        for (ClassInfo clazz : packageContent) {
            classes.add(create(Type.getType(Utils.descriptorFromClassName(clazz.getName())), false));
        }

        return classes;
    }

    public static int getCacheSize() {
        return _CAHCE.size();
    }

    public static ClassMetadata create(Type type) throws AnnotateException {
        return create(type, false);
    }

    public static ClassMetadata create(Type type, boolean allowNotFound) throws AnnotateException {
        if (_CAHCE.containsKey(type.getClassName())) {
            ClassMetadata meta = _CAHCE.get(type.getClassName());
            if (meta == null && !allowNotFound)
                throw new AnnotateException("Class '" + type.getClassName() + "' not found.");
            return meta;
        }
        ClassMetadata meta;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream classStream = classLoader.getResourceAsStream(type.getInternalName() + ".class");
            if (classStream == null) {
                if (allowNotFound) {
                    Annotate.LOG.warn("Could not find class '" + type.getInternalName() + "'.");
                    _CAHCE.put(type.getClassName(), null);
                    return null;
                }
                throw new AnnotateException("Class '" + type.getClassName() + "' not found.");
            }
            byte[] classFile = classStream.readAllBytes();
            ClassReader cr = new ClassReader(classFile);
            ClassMetadataVisitor visitor = new ClassMetadataVisitor(type);
            cr.accept(visitor, 0);
            meta = visitor.getTarget();
        } catch (IOException e) {
            throw new AnnotateException("Error while reading type metadata of '" + type.getClassName() + "'.", e);
        }
        _CAHCE.put(type.getClassName(), meta);
        return meta;
    }

    private static class ClassMetadataVisitor extends ClassVisitor implements Opcodes {
        private final Type _TYPE;
        private ClassMetadata _target;

        public ClassMetadataVisitor(Type type) {
            super(ASM9);
            _TYPE = type;
        }

        public ClassMetadata getTarget() {
            return _target;
        }

        @Override
        public void visit(final int version, final int access, final String name, final String signature,
                final String superName, final String[] interfaces) {
            _target = new ClassMetadata(_TYPE, superName, interfaces);
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
            AnnotationMetadata annotation = new AnnotationMetadata(Type.getType(descriptor));
            _target.addAnnotation(annotation);
            return new AnnotationMetadataVisitor(annotation);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                String signature, String[] exceptions) {
            MethodMetadata method = new MethodMetadata(_target, name, desc, access, exceptions);
            _target.addMethod(method);
            return new MethodMetadataVisitor(method);
        }

        class MethodMetadataVisitor extends MethodVisitor implements Opcodes {
            private final MethodMetadata _TARGET;

            protected MethodMetadataVisitor(MethodMetadata method) {
                super(ASM9);
                _TARGET = method;
            }

            @Override
            public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
                AnnotationMetadata annotation = new AnnotationMetadata(Type.getType(descriptor));
                _TARGET.addAnnotation(annotation);
                return new AnnotationMetadataVisitor(annotation);
            }

            @Override
            public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
                AnnotationMetadata annotation = new AnnotationMetadata(Type.getType(descriptor));
                _TARGET.addArgumentAnnotation(parameter, annotation);
                return new AnnotationMetadataVisitor(annotation);
            }

            @Override
            public void visitLocalVariable(String name, String descriptor,
                    String signature, Label start, Label end, int index) {
                // System.out.println("LOCAL " + index + " -> " + name + " " + descriptor + " RANGE " + start + " - " + end);
            }
        }

        class AnnotationMetadataVisitor extends AnnotationVisitor implements Opcodes {
            private final AnnotationMetadata _TARGET;

            protected AnnotationMetadataVisitor(AnnotationMetadata meta) {
                super(ASM9);
                _TARGET = meta;
            }

            @Override
            public void visit(String name, Object value) {
                _TARGET.addParam(name, value);
            }

            @Override
            public void visitEnum(final String name, final String descriptor, final String value) {
                _TARGET.addParam(name, new AnnotationEnumMetadata(descriptor, value));
            }

            @Override
            public AnnotationVisitor visitArray(String name) {
                AnnotationArrayMetadata meta = new AnnotationArrayMetadata();
                _TARGET.addParam(name, meta);
                return new AnnotationArrayMetadataVisitor(meta);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String name, String descriptor) {
                AnnotationMetadata meta = new AnnotationMetadata(Type.getType(descriptor));
                _TARGET.addParam(name, meta);
                return new AnnotationMetadataVisitor(meta);
            }
        }

        class AnnotationArrayMetadataVisitor extends AnnotationVisitor implements Opcodes {
            private final AnnotationArrayMetadata _TARGET;

            protected AnnotationArrayMetadataVisitor(AnnotationArrayMetadata array) {
                super(ASM9);
                _TARGET = array;
            }

            @Override
            public void visit(String name, Object value) {
                _TARGET.add(value);
            }

            @Override
            public void visitEnum(final String name, final String descriptor, final String value) {
                _TARGET.add(new AnnotationEnumMetadata(descriptor, value));
            }

            @Override
            public AnnotationVisitor visitAnnotation(String name, String descriptor) {
                AnnotationMetadata meta = new AnnotationMetadata(Type.getType(descriptor));
                _TARGET.add(meta);
                return new AnnotationMetadataVisitor(meta);
            }

            @Override
            public AnnotationVisitor visitArray(String name) {
                AnnotationArrayMetadata meta = new AnnotationArrayMetadata();
                _TARGET.add(meta);
                return new AnnotationArrayMetadataVisitor(meta);
            }
        }
    }
}
