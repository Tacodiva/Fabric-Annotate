package sh.emberj.annotate.alloy;

public class Return<T> {

    public final T value;

    private Return(T value) {
        this.value = value;
    }

    public static <T> Return<T> resume() {
        return null;
    }

    public static <T> Return<T> cancel(T returnValue) {
        return new Return<T>(returnValue);
    }
}
