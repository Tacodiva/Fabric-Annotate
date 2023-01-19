package sh.emberj.annotate.alloy;

public class ReturnVoid {

    private ReturnVoid() {
    }

    public static ReturnVoid resume() {
        return null;
    }

    public static ReturnVoid cancel() {
        return new ReturnVoid();
    }
}
