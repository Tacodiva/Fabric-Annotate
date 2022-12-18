package sh.emberj.annotate.resource;

import com.google.gson.JsonObject;

import sh.emberj.annotate.core.AnnotateException;

public class TestResource extends JsonGeneratedResource {

    public TestResource() {
        super("test.json");
    }
    
    @Override
    protected void appendJsonContents(JsonObject root) throws AnnotateException {
        root.addProperty("Hello", "World");        
    }
    
}
