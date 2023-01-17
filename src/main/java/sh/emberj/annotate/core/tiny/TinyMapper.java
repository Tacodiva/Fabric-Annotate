package sh.emberj.annotate.core.tiny;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;

import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.mapping.reader.v2.MappingParseException;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;
import sh.emberj.annotate.core.AnnotateException;

public class TinyMapper {

    private static TinyTree loadMappings() throws AnnotateException {
        final ClassLoader resourceLoader = Thread.currentThread().getContextClassLoader();
        URL resource = resourceLoader.getResource("mappings/annotate-map.tiny");
        if (resource == null)
            resource = resourceLoader.getResource("mappings/mappings.tiny");
        if (resource == null)
            throw new AnnotateException("Could not find any mapping files.");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()))) {
            return TinyMappingFactory.loadWithDetection(reader);
        } catch (MappingParseException | IOException e) {
            throw new AnnotateException("Encountered an unexpected exception while loading mappings.", e);
        }
    }

    private final Map<String, TinyNamespace> _NAMESPACES;
    private final String _CLASSPATH_NAMESPACE_ID;
    private final TinyNamespace _CLASSPATH_NAMESPACE;

    public TinyMapper() throws AnnotateException {
        _NAMESPACES = new HashMap<>();

        final TinyTree mapping = loadMappings();
        _CLASSPATH_NAMESPACE_ID = FabricLauncherBase.getLauncher().getTargetNamespace();
        _CLASSPATH_NAMESPACE = new TinyNamespace(this, mapping, _CLASSPATH_NAMESPACE_ID);
        _CLASSPATH_NAMESPACE.resolveInheritance();
        for (String namespace : mapping.getMetadata().getNamespaces()) {
            TinyNamespace namespaceObj;
            if (_CLASSPATH_NAMESPACE_ID.equals(namespace)) namespaceObj = _CLASSPATH_NAMESPACE;
            else namespaceObj = new TinyNamespace(this, mapping, namespace);
            namespaceObj.resolveInheritance();
            _NAMESPACES.put(namespaceObj.getId(), namespaceObj);
        }
    }

    public TinyNamespace getNamespace(String name) {
        return _NAMESPACES.get(name);
    }

    public TinyNamespace getClasspathNamespace() {
        if (_CLASSPATH_NAMESPACE == null)
            throw new RuntimeException("Classpath namespace mappings not avaliable.");
        return _CLASSPATH_NAMESPACE;
    }

    public String getClasspathNamespaceId() {
        return _CLASSPATH_NAMESPACE_ID;
    }

    public TinyMappedClass getClass(Class<?> clazz, TinyNamespace namespace) {
        return getClass(Type.getType(clazz), namespace);
    }

    public TinyMappedClass getClass(Type type, TinyNamespace namespace) {
        TinyMappedClass mappedClass = getClasspathNamespace().getClass(type.getInternalName());
        if (mappedClass == null)
            return null;
        return mappedClass.mapTo(namespace);
    }
}
