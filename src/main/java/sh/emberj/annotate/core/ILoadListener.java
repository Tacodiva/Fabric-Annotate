package sh.emberj.annotate.core;

public interface ILoadListener {

    public int getPriority();
    public AnnotateLoadStage getLoadStage();
    public void onLoad() throws AnnotateException;

}
