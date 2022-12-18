package sh.emberj.annotate.test;

import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import sh.emberj.annotate.networking.AnnotateNetCallbacks;
import sh.emberj.annotate.networking.NetworkContext;

public class CoolTesting {


    public static void run() {

        try {
            ClassReader reader = new ClassReader("sh.emberj.annotate.test.CoolTesting");
            TraceClassVisitor tcv = new TraceClassVisitor(new PrintWriter(System.out));
            reader.accept(tcv, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);

        AnnotateNetCallbacks.run(CoolTesting::another);

    }

    public static void myEvnetA(int info) {
    }

    public static void Abcde(EventInterface<?> whatever) {

    }

    public static void another(NetworkContext ctx) {

    }

    // public record myEventBData() extends EventContext {
    // }

    // public static <T extends EventContext> void runEvent(EventInterface<T> event, T arguments) {

    // }

    @FunctionalInterface
    public static interface EventInterface<T> {
        public void eventMethod(T ctx);
    }

}
