package sh.emberj.annotate.test;

import sh.emberj.annotate.core.Annotate;

public class MixinTarget {
    
    public static void staticOne() {
        Annotate.LOG.info("Static One!");
    }

    public static String staticTwo(String idk, int lmao) {
        Annotate.LOG.info("Static Two! " + lmao);
        Annotate.LOG.info(idk);
        return "original";
    }

    public double memberOne(double abcde) {
        Annotate.LOG.info("Member one! " + abcde);
        return 420;
    }

    public String state;

    public MixinTarget(String state) {
        this.state = state;
    }
}
