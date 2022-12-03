package sh.emberj.annotate.test;

import sh.emberj.annotate.registry.Register;

@Register(registry = AnimalRegistry.ID)
public class Sheep extends Animal {

    @Override
    public void makeNoise() {
        System.out.println("Baaahhhh!");        
    }


}
