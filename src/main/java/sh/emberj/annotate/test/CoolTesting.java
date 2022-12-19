package sh.emberj.annotate.test;

import java.io.PrintWriter;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import sh.emberj.annotate.core.AnnotateException;
import sh.emberj.annotate.networking.callback.ServerboundCallbackContext;

public class CoolTesting {

    public static class MyStringThing {
        public String value;

        public MyStringThing(String value) {
            this.value = value;
        }
    }

    public static Function<String, MyStringThing> easyCreateFactory() {
        return MyStringThing::new;
    }

    public static Function<String, Object> createFactory() throws AnnotateException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        try {
            return (Function<String, Object>) LambdaMetafactory.metafactory(
                    lookup,
                    
                    "apply",
                    MethodType.methodType(Function.class),

                    MethodType.methodType(MyStringThing.class, String.class).generic(),

                    lookup.findConstructor(
                            MyStringThing.class,
                            MethodType.methodType(void.class, String.class)),
                    MethodType.methodType(MyStringThing.class, String.class)
            ).getTarget().invokeExact();
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new AnnotateException(
                    "Error while creating packet factory. Make sure packet class has a public constructor with a single argument of type PacketByteBuf.",
                    e);
        } catch (Throwable e) {
            throw new AnnotateException(
                    "Unknown error while creating packet factory.", e);
        }
    }

    public static void run() {
        try {
            ClassReader reader = new ClassReader("sh.emberj.annotate.test.CoolTesting");
            TraceClassVisitor tcv = new TraceClassVisitor(new PrintWriter(System.out));
            reader.accept(tcv, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            Function<String, Object> func = createFactory();

            MyStringThing thing = (MyStringThing) func.apply("Hello!");
            System.out.println(thing.value);
        } catch (AnnotateException e) {
            e.showGUI();
        }

        System.exit(0);

        // NetCallbackRegistry.run(CoolTesting::another);

    }

    public static void myEvnetA(int info) {
    }

    public static void Abcde(EventInterface<?> whatever) {

    }

    public static void another(ServerboundCallbackContext ctx) {

    }

    // public record myEventBData() extends EventContext {
    // }

    // public static <T extends EventContext> void runEvent(EventInterface<T> event,
    // T arguments) {

    // }

    @FunctionalInterface
    public static interface EventInterface<T> {
        public void eventMethod(T ctx);
    }

}
