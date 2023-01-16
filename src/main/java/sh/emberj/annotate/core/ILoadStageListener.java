package sh.emberj.annotate.core;

public interface ILoadStageListener {

    public void onLoadStageChange(LoadStage newStage);

    public default int getPriority() {
        return 0;
    }
}
