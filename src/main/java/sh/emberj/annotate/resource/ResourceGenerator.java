package sh.emberj.annotate.resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.spongepowered.include.com.google.common.io.Files;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModOrigin;
import sh.emberj.annotate.core.Annotate;
import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateMod;
import sh.emberj.annotate.core.Utils;

public class ResourceGenerator {

    private static ResourceGenerator _instance;

    public static ResourceGenerator tryCreate(AnnotateMod mod) throws AnnotateException {
        if (mod.getResourceGenerationDirectory() == null)
            return null;
        if (!FabricLoader.getInstance().isDevelopmentEnvironment())
            return null;
        final ModOrigin origin = mod.getFabricMod().getOrigin();
        if (origin.getKind() != ModOrigin.Kind.PATH || origin.getPaths().size() == 0)
            return null;
        final Path modPath = origin.getPaths().get(0);
        final File modFile = modPath.toFile();
        if (!modFile.isDirectory())
            return null;
        final File resourceFile = new File(modFile, mod.getResourceGenerationDirectory());
        if (!resourceFile.isDirectory()) {
            Annotate.LOG.warn("Cannot generate resources for mod '" + mod.getID() + "'. '" + resourceFile
                    + "' does not exist or was not a directory.");
            return null;
        }
        Annotate.LOG.info("Found resources path '" + resourceFile + "' and class path '" + modFile + "' for mod '"
                + mod.getID() + "'.");
        if (_instance != null)
            return _instance;
        return _instance = new ResourceGenerator(modFile);
    }

    private final File _RESOURCES_FILE, _CLASSPATH_DIR;

    private ResourceGenerator(File resourcesFile) throws AnnotateException {
        _RESOURCES_FILE = resourcesFile;
        _CLASSPATH_DIR = new File(Annotate.getDirectory(), "resourcegen");
        try {
            FileUtils.deleteDirectory(_CLASSPATH_DIR);
        } catch (IOException e) {
            throw new AnnotateException("Error deleting resource gen directory.", e);
        }
        _CLASSPATH_DIR.mkdir();
        Utils.injectClasspathFile(_CLASSPATH_DIR);
    }

    public boolean hasResource(String path) {
        return new File(_RESOURCES_FILE, path).exists();
    }

    public void generate(IGeneratedResource resource) throws AnnotateException {
        final String path = resource.getPath();
        final byte[] resourceContents = resource.getContents();
        final File resPath = new File(_RESOURCES_FILE, path);
        final File modPath = new File(_CLASSPATH_DIR, path);

        try {
            Files.write(resourceContents, resPath);
            Files.write(resourceContents, modPath);
        } catch (IOException e) {
            throw new AnnotateException("Encountered exception while generating resources.", e);
        }
    }
}
