package sh.emberj.annotate.core;

import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.CustomValue.CvArray;
import net.fabricmc.loader.api.metadata.CustomValue.CvObject;
import net.fabricmc.loader.api.metadata.CustomValue.CvType;
import net.fabricmc.loader.api.metadata.ModMetadata;
import sh.emberj.annotate.resource.ResourceGenerator;

public class AnnotateMod {

    public static AnnotateMod tryCreate(ModContainer mod) throws AnnotateException {
        ModMetadata meta = mod.getMetadata();
        CustomValue cvRoot = meta.getCustomValue("annotate");

        if (cvRoot == null)
            return null;

        if (cvRoot.getType() != CvType.OBJECT)
            throw new AnnotateException("Invalid fabric.mod.json custom value 'annotate'. Value must be an object.",
                    mod);
        CvObject root = cvRoot.getAsObject();

        String[] packages;
        String resourceGenDir;
        {
            CustomValue valuePackages = root.get("packages");
            if (valuePackages == null)
                throw new AnnotateException(
                        "Invalid fabric.mod.json custom value 'annotate'. Object must contain the 'packages' key.",
                        mod);

            if (valuePackages.getType() == CvType.STRING) {
                packages = new String[] { valuePackages.getAsString() };
            } else if (valuePackages.getType() == CvType.ARRAY) {
                CvArray cvPackages = valuePackages.getAsArray();
                packages = new String[cvPackages.size()];
                for (int i = 0; i < cvPackages.size(); i++) {
                    CustomValue valuePackage = cvPackages.get(i);
                    if (valuePackage.getType() != CvType.STRING)
                        if (valuePackages == null)
                            throw new AnnotateException(
                                    "Invalid fabric.mod.json custom value 'annotate'. 'packages' array must contain strings only.",
                                    mod);
                    packages[i] = valuePackage.getAsString();
                }
            } else {
                throw new AnnotateException(
                        "Invalid fabric.mod.json custom value 'annotate'. 'packages' must be an array or string.", mod);
            }

            if (root.containsKey("resourcegen")) {
                CustomValue resourcegen = root.get("resourcegen");
                if (resourcegen.getType() != CvType.STRING)
                    throw new AnnotateException(
                            "Invalid fabric.mod.json custom value 'annotate'. 'resourcegen' must be a string.", mod);
                resourceGenDir = resourcegen.getAsString();
            } else {
                resourceGenDir = "../../src/main/resources";
            }
        }

        return new AnnotateMod(mod, packages, resourceGenDir);
    }

    private final ModContainer _MOD;
    private final String[] _PACKAGES;
    private final String _RESOURCE_GEN_DIR;
    private final ResourceGenerator _RESOURCE_GENERATOR;

    public AnnotateMod(ModContainer mod, String[] packages, String resourceGenDir) throws AnnotateException {
        _MOD = mod;
        _PACKAGES = packages;
        _RESOURCE_GEN_DIR = resourceGenDir;
        _RESOURCE_GENERATOR = ResourceGenerator.tryCreate(this);
    }

    public String[] getPackages() {
        return _PACKAGES;
    }

    public ModContainer getFabricMod() {
        return _MOD;
    }

    public String getID() {
        return _MOD.getMetadata().getId();
    }

    @Override
    public String toString() {
        return getID();
    }

    public String getResourceGenerationDirectory() {
        return _RESOURCE_GEN_DIR;
    }

    public ResourceGenerator getResourceGenerator() {
        return _RESOURCE_GENERATOR;
    }

}
