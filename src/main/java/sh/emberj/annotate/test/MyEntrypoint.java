package sh.emberj.annotate.test;

import sh.emberj.annotate.core.AnnotateLoadStage;
import sh.emberj.annotate.entrypoint.Entrypoint;

public class MyEntrypoint {

    @Entrypoint(stage = AnnotateLoadStage.PRELAUNCH)
    public static void onPreLaunch() {
        // ...
    }
    
}

