package sh.emberj.annotate.core.tiny;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;

import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.TinyTree;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.asm.ClassMetadata;
import sh.emberj.annotate.core.asm.ClassMetadataFactory;

public class TinyNamespace {

    private final String _ID;
    private Map<String, TinyMappedClass> _CLASSES;

    TinyNamespace(TinyMapper mapper, TinyTree mapping, String id) throws AnnotateException {
        _ID = id;
        _CLASSES = new HashMap<>();
        for (ClassDef classDef : mapping.getClasses()) {
            ClassMetadata typeMeta = ClassMetadataFactory.create(
                    Type.getType("L" + classDef.getName(mapper.getClasspathNamespaceId()) + ";"),
                    true);
            if (typeMeta != null) {
                TinyMappedClass clazz = new TinyMappedClass(mapper, typeMeta, classDef, this);
                _CLASSES.put(clazz.getName(), clazz);
            }
        }
    }

    void resolveInheritance() {
        for (TinyMappedClass clazz : getClasses())
            clazz.resolveInheritance();
    }

    public String getId() {
        return _ID;
    }

    public Collection<TinyMappedClass> getClasses() {
        return _CLASSES.values();
    }

    public TinyMappedClass getClass(String name) {
        return _CLASSES.get(name);
    }

    public TinyMappedClass getClass(Type type) {
        return getClass(type.getInternalName());
    }
}
