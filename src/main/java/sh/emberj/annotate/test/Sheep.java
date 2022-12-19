package sh.emberj.annotate.test;

@RegisterAnimal
public class Sheep extends Animal {

    @Override
    public void makeNoise() {
        System.out.println("Baaahhhh!");        
    }


}
