package sh.emberj.annotate.resource;

import com.google.gson.JsonObject;

import sh.emberj.annotate.core.AnnotateException;

public abstract class JsonGeneratedResource implements IGeneratedResource {

    private final String _PATH;

    public JsonGeneratedResource(String path) {
        _PATH = path;
    }

    @Override
    public String getPath() {
        return _PATH;
    }

    @Override
    public byte[] getContents() throws AnnotateException {
        JsonObject obj = new JsonObject();
        appendJsonContents(obj);
        return obj.toString().getBytes();
    }

    protected abstract void appendJsonContents(JsonObject root) throws AnnotateException;
}
