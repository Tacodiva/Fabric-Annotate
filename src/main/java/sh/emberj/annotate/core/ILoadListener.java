package sh.emberj.annotate.core;

public interface ILoadListener {

    public int getPriority();
    public FabricLoadStage getLoadStage();
    public void onLoad() throws AnnotateException;

}
