package sh.emberj.annotate.test;

import sh.emberj.annotate.core.Annotate;

public class MixinTarget {
    
    public static void staticOne() {
        int localOne = 69;
        double localTwo = 7729;
        int localThree = 420;
        String innerLocal;
        if (localThree < 10) {
            innerLocal = "Less ";
            int ten = 10;
            System.out.println(innerLocal + ten);
        } else {
            int five = 5;
            System.out.println(five);
        }

        double localFour = 21892;
        Annotate.LOG.info("Static One!");
    }

    // public static String staticTwo(String idk, int lmao) {
    //     Annotate.LOG.info("Static Two! " + lmao);
    //     Annotate.LOG.info(idk);
    //     return "original";
    // }

    // public double memberOne(double abcde) {
    //     Annotate.LOG.info("Member one! " + abcde);
    //     return 420;
    // }

    // public String state;

    // public MixinTarget(String state) {
    //     this.state = state;
    // }
}
