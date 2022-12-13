package sh.emberj.annotate.entrypoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateScan;
import sh.emberj.annotate.core.LoadStage;

@AnnotateScan
public class EntrypointManager {

    private EntrypointManager() {
    }

    private static Map<LoadStage, List<EntrypointInstance>> _ENTRYPOINTS = new HashMap<>();

    static void addEntrypoint(EntrypointInstance instance) {
        List<EntrypointInstance> instances = _ENTRYPOINTS.get(instance.getLoadStage());
        if (instances == null) {
            instances = new ArrayList<>();
            _ENTRYPOINTS.put(instance.getLoadStage(), instances);
        }
        instances.add(instance);
    }

    public static void invokeEntrypoints(LoadStage stage) {
        List<EntrypointInstance> instances = _ENTRYPOINTS.get(stage);
        if (instances != null) {
            instances.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));
            for (EntrypointInstance instance : instances) {
                try {
                    instance.tryInvoke();
                } catch (AnnotateException e) {
                    e.trySet(instance.getMethod());
                    e.showGUI();
                }
            }
        }
    }
}
