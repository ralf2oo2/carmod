package ralf2oo2.carmod;

import net.minecraft.entity.player.PlayerEntity;
import net.modificationstation.stationapi.api.tick.TickScheduler;
import org.lwjgl.opengl.GL11;
import org.ode4j.math.DMatrix3;
import org.ode4j.math.DMatrix3C;
import org.ode4j.math.DVector3;
import org.ode4j.ode.*;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;
import ralf2oo2.carmod.client.render.DebugRenderer;
import ralf2oo2.carmod.entity.CarEntity;
import ralf2oo2.carmod.registry.VehicleRegistry;
import ralf2oo2.carmod.vehicle.Vehicle;
import ralf2oo2.carmod.vehicle.VehicleModel;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.ode4j.ode.OdeConstants.*;
import static org.ode4j.ode.internal.DxGeom.dCollide;

public class PhysicsEngine implements Runnable{
    private static final int MAX_CONTACTS = 8;
    private final DWorld world;
    private final DSpace space;
    private final DJointGroup contactGroup;
    private final Map<CarEntity, DBody> collisionBodies = new HashMap<>();
    private final List<CarEntity> removalQueue = new ArrayList<>();
    public final Queue<Runnable> executionQueue = new ConcurrentLinkedQueue<>();

    public PhysicsEngine(){
        OdeHelper.initODE2(0);
        world = OdeHelper.createWorld();
        world.setGravity (0,-9.81,0);
        contactGroup = OdeHelper.createJointGroup();
        space = OdeHelper.createSimpleSpace();
        world.setContactSurfaceLayer (0.001);
        OdeHelper.createPlane(space, 0, 1, 0, 65);
    }

    public Optional<DBody> getEntityBody(CarEntity entity){
        if(!collisionBodies.containsKey(entity)) return Optional.empty();
        return Optional.ofNullable(collisionBodies.get(entity));
    }

    public void registerEntity(CarEntity entity){
        if(collisionBodies.containsKey(entity)) return;
        if(entity.carName == null) return;
        Optional<Vehicle> vehicle = VehicleRegistry.getVehicle(entity.carName);
        if(!vehicle.isPresent()) return;
        DBody body = OdeHelper.createBody(world);
        body.setPosition(entity.x, entity.y, entity.z);
        RenderwareBinaryStream.Col collisions = vehicle.get().vehicleCollisions.collisions;
        for(int i = 0; i < collisions.spheres().size(); i++){
            RenderwareBinaryStream.ColSphere sphere = collisions.spheres().get(i);
            DGeom geom = OdeHelper.createSphere(space, sphere.radius());
            geom.setBody(body);
            geom.setOffsetPosition(sphere.center().x(), sphere.center().z(), sphere.center().y());
        }
        DMass mass = OdeHelper.createMass();
        RenderwareBinaryStream.ColBounds bounds = collisions.header().bounds();
        mass.setBoxTotal(1500d, bounds.max().z() - bounds.min().z(), bounds.max().x() - bounds.min().x(), bounds.max().y() - bounds.min().y());
        body.setMass(mass);
        collisionBodies.put(entity, body);
    }

    public void pollQueue(){
        Runnable command;
        while ((command = executionQueue.poll()) != null) command.run();
    }

    @Override
    public void run() {
        handleCollisions();
        world.quickStep(1d / 60);
        pollQueue();
        updateEntities();
        removeDeadEntities();
        contactGroup.empty();
    }

    private void handleCollisions(){
        space.collide(space, nearCallback);
    }

    private void updateEntities(){
        for (Map.Entry<CarEntity, DBody> entry : collisionBodies.entrySet()) {
            TickScheduler.CLIENT_RENDER_START.immediate(() -> {
                CarEntity entity = entry.getKey();
                DBody body = entry.getValue();
                if(entity.isAlive()){
                    entity.setPosition(body.getPosition().get(0), body.getPosition().get(1), body.getPosition().get(2));

                    DMatrix3C rotationMatrix = body.getRotation();

                    entity.setRotationMatrix(rotationMatrix.toFloatArray());
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

    private final DGeom.DNearCallback nearCallback = this::nearCallback;
    private void nearCallback (Object data, DGeom o1, DGeom o2)
    {
        int i;
        // if (o1->body && o2->body) return;

        // exit without doing anything if the two bodies are connected by a joint
        DBody b1 = o1.getBody();
        DBody b2 = o2.getBody();
        if (b1!=null && b2!=null && OdeHelper.areConnectedExcluding (b1,b2,DContactJoint.class)) {
            return;
        }

        //dContact[] contact=new dContact[MAX_CONTACTS];   // up to MAX_CONTACTS contacts per box-box
        DContactBuffer contacts = new DContactBuffer(MAX_CONTACTS);
        for (DContact contact: contacts) {
            contact.surface.mode = dContactBounce | dContactSoftCFM;
            contact.surface.soft_cfm = 1e-4;
            contact.surface.mu = 0.8d;
        }
        //	if (int numc = dCollide (o1,o2,MAX_CONTACTS,&contact[0].geom,
        //			sizeof(dContact))) {
        int numc = OdeHelper.collide (o1,o2,MAX_CONTACTS,contacts.getGeomBuffer());//, sizeof(dContact));
        if (numc!=0) {
            DMatrix3 RI = new DMatrix3();
            RI.setIdentity();
            final DVector3 ss = new DVector3(0.02,0.02,0.02);
            for (i=0; i<numc; i++) {
                DJoint c = OdeHelper.createContactJoint (world,contactGroup,contacts.get(i));
                c.attach (b1,b2);
            }
        }
    }
}
