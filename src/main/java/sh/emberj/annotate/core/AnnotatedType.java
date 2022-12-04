package sh.emberj.annotate.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.google.common.reflect.TypeToken;

import sh.emberj.annotate.core.asm.AnnotatedTypeMeta;

public class AnnotatedType {

    private final AnnotatedMod _MOD;
    private final TypeToken<?> _TYPE;

    private Object _instance;
    private AnnotatedTypeMeta _meta;

    public AnnotatedType(Class<?> typeClass) {
        _TYPE = TypeToken.of(typeClass);

        _MOD = Annotate.getInstance().findModFromPackage(getRawType().getPackageName());
        _MOD.addType(this);
    }

    public AnnotatedMod getMod() {
        return _MOD;
    }

    public TypeToken<?> getType() {
        return _TYPE;
    }

    public Class<?> getRawType() {
        return _TYPE.getRawType();
    }

    public Object getInstance() {
        return _instance;
    }

    public AnnotatedTypeMeta getMeta() throws AnnotateException {
        if (_meta != null) return _meta;
        return _meta = AnnotatedTypeMeta.readMetadata(_TYPE.getType().getTypeName());
    }

    public void setInstance(Object instance) throws AnnotateException {
        if (_instance != null) throw new AnnotateException("Cannot set instance twice!");
        if (getRawType() != instance.getClass())
            throw new AnnotateException("'" + instance.getClass() + "'' not the same type as '" + _TYPE + "'.");
        this._instance = instance;
        for (Field field : getRawType().getDeclaredFields()) {
            if (field.getAnnotation(Instance.class) != null) {
                if (!Modifier.isStatic(field.getModifiers()))
                    throw new AnnotateException("Fields annotated with @Instance must be static.", field.getName(),
                            this);
                try {
                    field.set(null, instance);
                } catch (IllegalArgumentException e) {
                    throw new AnnotateException(
                            "Fields annotated with @Instance must have the same type as their declaring type.",
                            field.getName(), this);
                } catch (IllegalAccessException e) {
                    if (Modifier.isFinal(field.getModifiers()))
                        throw new AnnotateException("Fields annotated with @Instance cannot be final.", field.getName(),
                                this);
                    else
                        throw new AnnotateException("Fields annotated with @Instance must be public.", field.getName(),
                                this);
                }
                break;
            }
        }
    }

    @Override
    public String toString() {
        return _TYPE.toString();
    }
}
