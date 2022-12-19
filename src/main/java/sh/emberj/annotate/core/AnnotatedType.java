package sh.emberj.annotate.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.objectweb.asm.Type;

import com.google.common.reflect.TypeToken;

import sh.emberj.annotate.core.asm.AnnotatedTypeMeta;

public class AnnotatedType {

    private static Field findInstanceField(AnnotatedType type) throws AnnotateException {
        Field instanceField = null;
        for (Field field : type.getAsClass().getDeclaredFields()) {
            if (field.getAnnotation(Instance.class) != null) {
                if (instanceField != null)
                    throw new AnnotateException("Types can only have one field annotated with @Instance. Found '"
                            + instanceField.getName() + "' and '" + field.getName() + "'.", field.getName(), type);
                if (!field.getType().equals(type.getAsClass()))
                    throw new AnnotateException(
                            "Instance fields must be of the same type as their declairing type. Expected "
                                    + type.getAsClass() + " got " + field.getType() + ".",
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

    private final AnnotatedMod _MOD;
    private final TypeToken<?> _TYPE;
    private final Class<?> _CLASS;
    private final AnnotatedTypeMeta _META;
    private final Field _INSTANCE_FIELD;

    private Object _instance;

    public AnnotatedType(Class<?> typeClass) throws AnnotateException {
        _TYPE = TypeToken.of(typeClass);
        _CLASS = typeClass;
        _META = AnnotatedTypeMeta.readMetadata(Type.getType(_CLASS));

        _MOD = Annotate.getInstance().findModFromPackage(getAsClass().getPackageName());
        _MOD.addType(this);

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

    public AnnotatedMod getMod() {
        return _MOD;
    }

    public TypeToken<?> getType() {
        return _TYPE;
    }

    public Class<?> getAsClass() {
        return _CLASS;
    }

    public AnnotatedTypeMeta getMeta() {
        return _META;
    }

    public Object getInstance() throws AnnotateException {
        if (_instance != null)
            return _instance;

        try {
            _instance = _CLASS.getConstructor().newInstance();
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

    @Override
    public String toString() {
        return _TYPE.toString();
    }
}
