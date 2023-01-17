package sh.emberj.annotate.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import sh.emberj.annotate.core.asm.ClassMetadata;

public class AnnotatedClass {

    private static Field findInstanceField(AnnotatedClass type) throws AnnotateException {
        Field instanceField = null;
        for (Field field : type.getMetadata().getAsClass().getDeclaredFields()) {
            if (field.getAnnotation(Instance.class) != null) {
                if (instanceField != null)
                    throw new AnnotateException("Types can only have one field annotated with @Instance. Found '"
                            + instanceField.getName() + "' and '" + field.getName() + "'.", field.getName(), type);
                if (!field.getType().equals(type.getMetadata().getAsClass()))
                    throw new AnnotateException(
                            "Instance fields must be of the same type as their declairing type. Expected "
                                    + type.getMetadata().getAsClass() + " got " + field.getType() + ".",
                            field.getName(), type);
                if (!Modifier.isStatic(field.getModifiers()))
                    throw new AnnotateException("Fields annotated with @Instance must be static.", field.getName(),
                            type);
                if (!Modifier.isPublic(field.getModifiers()))
                    throw new AnnotateException("Fields annotated with @Instance must be public.", field.getName(),
                            type);
                instanceField = field;
            }
        }
        return instanceField;
    }

    private final ClassMetadata _METADATA;
    private final AnnotateMod _MOD;

    private final Field _INSTANCE_FIELD;
    private Object _instance;

    public AnnotatedClass(AnnotateMod mod, ClassMetadata metadata)
            throws AnnotateException {
        _METADATA = metadata;
        _MOD = mod;

        _INSTANCE_FIELD = findInstanceField(this);
        if (_INSTANCE_FIELD != null) {
            try {
                _instance = _INSTANCE_FIELD.get(null);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new AnnotateException("Error getting value of instance field.", e);
            }
            if (_instance == null && Modifier.isFinal(_INSTANCE_FIELD.getModifiers())) {
                throw new AnnotateException("Final instance fields must be initalized to a value.",
                        _INSTANCE_FIELD.getName(), this);
            }
        }
    }

    public Object getInstance() throws AnnotateException {
        if (_instance != null)
            return _instance;

        try {
            _instance = getMetadata().getAsClass().getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new AnnotateException(
                    "Failed to create instance. Could not find empty constructor. Make sure type has a public constructor that takes no parameters.",
                    this);
        } catch (InvocationTargetException e) {
            throw new AnnotateException(
                    "Exception encountered from blank constructor.", this, e.getCause());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
            throw new AnnotateException(
                    "Exception encountered while creating instance.", this, e);
        }

        if (_INSTANCE_FIELD != null) {
            try {
                _INSTANCE_FIELD.set(null, _instance);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new AnnotateException("Error setting value of instance field.", e);
            }
        }

        return _instance;
    }

    @SuppressWarnings("unchecked")
    public <T> T tryCastInstance(Class<T> class_) throws AnnotateException {
        final Object instance = getInstance();
        if (class_.isAssignableFrom(instance.getClass()))
            return (T) instance;
        return null;
    }

    public ClassMetadata getMetadata() {
        return _METADATA;
    }

    public AnnotateMod getMod() {
        return _MOD;
    }
}
