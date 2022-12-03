package sh.emberj.annotate.core;

import java.util.HashSet;
import java.util.Set;

import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.CustomValue.CvArray;
import net.fabricmc.loader.api.metadata.CustomValue.CvObject;
import net.fabricmc.loader.api.metadata.CustomValue.CvType;
import net.fabricmc.loader.api.metadata.ModMetadata;

public class AnnotatedMod {

    public static AnnotatedMod tryCreate(ModContainer mod) throws AnnotateException {
        ModMetadata meta = mod.getMetadata();
        CustomValue cvRoot = meta.getCustomValue("annotate");

        if (cvRoot == null) return null;

        if (cvRoot.getType() != CvType.OBJECT)
            throw new AnnotateException("Invalid fabric.mod.json custom value 'annotate'. Value must be an object.",
                    mod);
        CvObject root = cvRoot.getAsObject();

        String[] packages;
        {
            CustomValue valuePackages = root.get("packages");
            if (valuePackages == null) throw new AnnotateException(
                    "Invalid fabric.mod.json custom value 'annotate'. Object must contain the 'packages' key.", mod);

            if (valuePackages.getType() == CvType.STRING) {
                packages = new String[] { valuePackages.getAsString() };
            } else if (valuePackages.getType() == CvType.ARRAY) {
                CvArray cvPackages = valuePackages.getAsArray();
                packages = new String[cvPackages.size()];
                for (int i = 0; i < cvPackages.size(); i++) {
                    CustomValue valuePackage = cvPackages.get(i);
                    if (valuePackage.getType() != CvType.STRING) if (valuePackages == null) throw new AnnotateException(
                            "Invalid fabric.mod.json custom value 'annotate'. 'packages' array must contain strings only.",
                            mod);
                    packages[i] = valuePackage.getAsString();
                }
            } else {
                throw new AnnotateException(
                        "Invalid fabric.mod.json custom value 'annotate'. 'packages' must be an array or string.", mod);
            }
        }

        return new AnnotatedMod(mod, packages);
    }

    private final ModContainer _MOD;
    private final String[] _PACKAGES;
    private final Set<AnnotatedType> _TYPES;
    private final Set<AnnotatedMethod> _METHODS;

    public AnnotatedMod(ModContainer mod, String[] packages) {
        _MOD = mod;
        _PACKAGES = packages;
        _TYPES = new HashSet<>();
        _METHODS = new HashSet<>();
    }

    public ModContainer getModContainer() {
        return _MOD;
    }

    public String getName() {
        return _MOD.getMetadata().getName();
    }

    public String getId() {
        return _MOD.getMetadata().getId();
    }

    public String[] getPackages() {
        return _PACKAGES;
    }

    public boolean containsPackage(String packageName) {
        for (String modPackage : _PACKAGES) {
            if (!packageName.startsWith(modPackage)) continue;
            if (modPackage.length() == packageName.length() || packageName.charAt(modPackage.length()) == '.')
                return true;
        }
        return false;
    }

    void addType(AnnotatedType type) {
        _TYPES.add(type);
    }

    void addMethod(AnnotatedMethod method) {
        _METHODS.add(method);
    }
}
