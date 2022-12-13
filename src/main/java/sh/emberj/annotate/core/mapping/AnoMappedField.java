package sh.emberj.annotate.core.mapping;

import net.fabricmc.mapping.tree.FieldDef;

public class AnoMappedField {
    private final FieldDef _FIELD_DEF;
    private final AnoMappedClass _CLASS;
    private final String _NAME;
    
    AnoMappedField(FieldDef fieldDef, AnoMappedClass clazz) {
        _FIELD_DEF = fieldDef;
        _CLASS = clazz;
        _NAME = _FIELD_DEF.getName(getNamespace().getId());
        
    }

    public AnoMappedField mapTo(AnoNamespace namespace) {
        if (getNamespace() == namespace) return this;
        return _CLASS.mapTo(namespace).getField(mapNameTo(namespace));
    }

    public String mapNameTo(AnoNamespace namespace) {
        if (getNamespace() == namespace) return _NAME;
        return _FIELD_DEF.getName(namespace.getId());
    }

    public String getName() {
        return _NAME;
    }

    public AnoNamespace getNamespace() {
        return _CLASS.getNamespace();
    }

}
