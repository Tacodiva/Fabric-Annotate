package sh.emberj.annotate.networking;

public class AnnotateNetCallbacks {
    private AnnotateNetCallbacks() {
    }

    public static <T> void run(NetworkCallbacks.Both<T> callback) {
        
    }

    public static void run(NetworkCallbacks.CtxOnly callback) {

    }

    public static <T> void run(NetworkCallbacks.ParamOnly<T> callback) {

    }

    public static void run(NetworkCallbacks.Neither callback) {

    }
}
