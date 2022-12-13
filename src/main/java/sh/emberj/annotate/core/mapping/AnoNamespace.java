package sh.emberj.annotate.core.mapping;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;

import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.TinyTree;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.asm.AnnotatedTypeMeta;

public class AnoNamespace {

    private final String _ID;
    private Map<String, AnoMappedClass> _CLASSES;

    AnoNamespace(AnoMapper mapper, TinyTree mapping, String id) throws AnnotateException {
        _ID = id;
        _CLASSES = new HashMap<>();
        for (ClassDef classDef : mapping.getClasses()) {
            AnnotatedTypeMeta typeMeta = AnnotatedTypeMeta.readMetadata(
                    Type.getType("L" + classDef.getName(mapper.getClasspathNamespaceId()) + ";"),
                    true);
            if (typeMeta != null) {
                AnoMappedClass clazz = new AnoMappedClass(mapper, typeMeta, classDef, this);
                _CLASSES.put(clazz.getName(), clazz);
            }
        }
    }

    void resolveInheritance() {
        for (AnoMappedClass clazz : getClasses())
            clazz.resolveInheritance();
    }

    public String getId() {
        return _ID;
    }

    public Collection<AnoMappedClass> getClasses() {
        return _CLASSES.values();
    }

    public AnoMappedClass getClass(String name) {
        return _CLASSES.get(name);
    }

    public AnoMappedClass getClass(Type type) {
        return getClass(type.getInternalName());
    }
}
