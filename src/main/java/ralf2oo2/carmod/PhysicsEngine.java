package ralf2oo2.carmod;

import net.modificationstation.stationapi.api.tick.TickScheduler;
import org.ode4j.ode.*;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;
import ralf2oo2.carmod.entity.CarEntity;
import ralf2oo2.carmod.registry.VehicleRegistry;
import ralf2oo2.carmod.vehicle.Vehicle;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PhysicsEngine implements Runnable{
    private final DWorld world;
    private final Map<CarEntity, DBody> collisionBodies = new HashMap<>();
    private final List<CarEntity> removalQueue = new ArrayList<>();
    public final Queue<Runnable> executionQueue = new ConcurrentLinkedQueue<>();

    public PhysicsEngine(){
        OdeHelper.initODE2(0);
        world = OdeHelper.createWorld();
        world.setGravity (0,-0.5,0);
    }

    public void registerEntity(CarEntity entity){
        if(collisionBodies.containsKey(entity)) return;
        DBody body = OdeHelper.createBody(world);
        body.setPosition(entity.x, entity.y, entity.z);
        DSpace space = OdeHelper.createSimpleSpace();
        DGeom geom = OdeHelper.createBox(space, 1, 1, 1);
        geom.setBody(body);
        collisionBodies.put(entity, body);
    }

    public void pollQueue(){
        Runnable command;
        while ((command = executionQueue.poll()) != null) command.run();
    }

    @Override
    public void run() {
        world.step(1d / 60);
        pollQueue();
        updateEntities();
        removeDeadEntities();
    }

    private void updateEntities(){
        for (Map.Entry<CarEntity, DBody> entry : collisionBodies.entrySet()) {
            TickScheduler.CLIENT_RENDER_START.immediate(() -> {
                CarEntity entity = entry.getKey();
                DBody body = entry.getValue();
                if(entity.isAlive()){
                    entity.setPosition(body.getPosition().get(0), body.getPosition().get(1), body.getPosition().get(2));
                }
                else {
                    removalQueue.add(entity);
                }
            });
        }
    }

    private void removeDeadEntities(){
        for(CarEntity entity : removalQueue){
            DBody body = collisionBodies.get(entity);
            collisionBodies.remove(entity);
            body.destroy();
        }
    }
}
