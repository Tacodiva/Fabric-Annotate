package sh.emberj.annotate.entrypoint;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.core.AnnotateScan;
import sh.emberj.annotate.core.AnnotatedMethod;
import sh.emberj.annotate.core.AnnotatedMethodHandler;
import sh.emberj.annotate.core.LoadStage;
import sh.emberj.annotate.core.asm.AnnotatedMethodMeta;
import sh.emberj.annotate.core.asm.AnnotationMeta;

@AnnotateScan
public class EntrypointMethodHandler extends AnnotatedMethodHandler {

    public EntrypointMethodHandler() {
        super(LoadStage.PRELAUNCH, 1000);
    }

    @Override
    public void handle(AnnotatedMethod method) throws AnnotateException {
        AnnotatedMethodMeta meta = method.getMeta();
        AnnotationMeta annotation = meta.getAnnotationByType(Entrypoint.class);

        if (annotation != null) {            
            Integer priorityObj = (Integer) annotation.getParam("priority");
            int priority;
            if (priorityObj == null) priority = 0;
            else priority = priorityObj;
            LoadStage stage = annotation.getEnumParam("stage", LoadStage.class);
            if (stage == null) stage = LoadStage.INIT;
            EntrypointInstance entrypoint = new EntrypointInstance(stage, priority, method);
            EntrypointManager.addEntrypoint(entrypoint);
        }
    }
}
