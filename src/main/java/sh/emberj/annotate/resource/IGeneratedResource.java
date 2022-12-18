package sh.emberj.annotate.resource;

import sh.emberj.annotate.core.AnnotateException;

public interface IGeneratedResource {

    public String getPath();
    public byte[] getContents() throws AnnotateException;

}
