package sh.emberj.annotate.core.tiny;

import net.fabricmc.mapping.tree.FieldDef;

public class TinyMappedField {
    private final FieldDef _FIELD_DEF;
    private final TinyMappedClass _CLASS;
    private final String _NAME;
    
    TinyMappedField(FieldDef fieldDef, TinyMappedClass clazz) {
        _FIELD_DEF = fieldDef;
        _CLASS = clazz;
        _NAME = _FIELD_DEF.getName(getNamespace().getId());
        
    }

    public TinyMappedField mapTo(TinyNamespace namespace) {
        if (getNamespace() == namespace) return this;
        return _CLASS.mapTo(namespace).getField(mapNameTo(namespace));
    }

    public String mapNameTo(TinyNamespace namespace) {
        if (getNamespace() == namespace) return _NAME;
        return _FIELD_DEF.getName(namespace.getId());
    }

    public String getName() {
        return _NAME;
    }

    public TinyNamespace getNamespace() {
        return _CLASS.getNamespace();
    }

}
