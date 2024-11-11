package ralf2oo2.carmod.physics;

import org.ode4j.ode.DBody;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import ralf2oo2.carmod.entity.CarEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class PhysicsObject {
    public final CarEntity carEntity;
    public final DWorld world;
    public final DSpace space;
    public final List<DBody> bodies = new ArrayList<>();

    protected PhysicsObject(CarEntity carEntity, DWorld world, DSpace space) {
        this.carEntity = carEntity;
        this.world = world;
        this.space = space;
    }

    public abstract void update();
}
