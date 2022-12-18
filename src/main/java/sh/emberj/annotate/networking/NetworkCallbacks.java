package sh.emberj.annotate.networking;

public class NetworkCallbacks {
    private NetworkCallbacks() {
    }

    @FunctionalInterface
    public interface Both<T> {
        public void invoke(NetworkContext ctx, T param);
    }

    @FunctionalInterface
    public interface CtxOnly {
        public void invoke(NetworkContext ctx);
    }

    @FunctionalInterface
    public interface ParamOnly<T> {
        public void invoke(T param);
    }

    @FunctionalInterface
    public interface Neither {
        public void invoke();
    }
}
