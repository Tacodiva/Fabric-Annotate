package sh.emberj.annotate.test;

import sh.emberj.annotate.registry.Register;

@Register(value = AnimalRegistry.ID, path = "piglet")
@Register(value = AnimalRegistry.ID)
public class Pig extends Animal {

    @Override
    public void makeNoise() {
        System.out.println("Oink!");
    }

}
