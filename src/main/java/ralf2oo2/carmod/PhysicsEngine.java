package ralf2oo2.carmod;

import net.modificationstation.stationapi.api.tick.TickScheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.util.vector.Vector3f;
import org.ode4j.math.*;
import org.ode4j.ode.*;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;
import ralf2oo2.carmod.entity.CarEntity;
import ralf2oo2.carmod.registry.VehicleRegistry;
import ralf2oo2.carmod.vehicle.Vehicle;
import ralf2oo2.carmod.vehicle.VehicleModel;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PhysicsEngine implements Runnable{
    private static final Logger logger = LogManager.getLogger(PhysicsEngine.class);
    private static final int MAX_CONTACTS = 10;
    private static final DVector3C yunit = new DVector3(0, 1, 0);
    private static final DVector3C zunit = new DVector3(0, 0, 1);
    private final DWorld world;
    private final DSpace space;
    private final DJointGroup contactGroup;
    private final Map<CarEntity, DBody[]> collisionBodies = new HashMap<>();
    private final List<CarEntity> removalQueue = new ArrayList<>();
    public final Queue<Runnable> executionQueue = new ConcurrentLinkedQueue<>();

    public PhysicsEngine(){
        OdeHelper.initODE2(0);
        world = OdeHelper.createWorld();
        world.setGravity (0,-9.81,0);
        contactGroup = OdeHelper.createJointGroup();
        space = OdeHelper.createSimpleSpace();
        world.setContactSurfaceLayer (0.001);
        OdeHelper.createPlane(space, 0, 1, 0, 64);
    }

    public Optional<DBody[]> getEntityBodies(CarEntity entity){
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
        DGeom meshGeom = OdeHelper.createTriMesh(space, vehicle.get().vehicleCollisions.meshCollider);
        meshGeom.setBody(body);
        DMass mass = OdeHelper.createMass();
        RenderwareBinaryStream.ColBounds bounds = collisions.header().bounds();
        mass.setBoxTotal(1500d, bounds.max().z() - bounds.min().z(), bounds.max().x() - bounds.min().x(), bounds.max().y() - bounds.min().y());
        body.setMass(mass);

        List<RenderwareBinaryStream.Frame> wheelFrames = getWheelFrames(vehicle.get().vehicleModel);

        DBody[] wheelBodies = new DBody[wheelFrames.size()];
        DHinge2Joint[] wheelJoints = new DHinge2Joint[wheelFrames.size()];

        DMass wheelMass = OdeHelper.createMass();
        wheelMass.setSphere(1, 1);

        for(int i = 0; i < wheelFrames.size(); i++){
            wheelBodies[i] = OdeHelper.createBody(world);
            DQuaternion q = new DQuaternion();
            OdeMath.dQFromAxisAndAngle (q,1,0,0,Math.PI*0.5);
            wheelBodies[i].setQuaternion(q);
            wheelBodies[i].setMass(wheelMass);

            DGeom sphere = OdeHelper.createSphere(space, 0.7d);
            sphere.setBody(wheelBodies[i]);

            Vector3f wheelOffset = vehicle.get().vehicleModel.getFrameOffset(wheelFrames.get(i));
            wheelBodies[i].setPosition(wheelOffset.x + body.getPosition().get0(), wheelOffset.y + body.getPosition().get1(), wheelOffset.z + body.getPosition().get2());

            wheelJoints[i] = OdeHelper.createHinge2Joint(world, null);
            wheelJoints[i].attach(body, wheelBodies[i]);

            final DVector3C a = wheelBodies[i].getPosition();
            DHinge2Joint h2 = wheelJoints[i];
            h2.setAnchor(a);
            h2.setAxes (zunit, yunit);

            wheelJoints[i].setParamSuspensionERP(0.9);
            wheelJoints[i].setParamSuspensionCFM(0.1);

            if(vehicle.get().vehicleModel.getFrameName(wheelFrames.get(i)).substring(7).startsWith("b")){
                wheelJoints[i].setParamLoStop(0);
                wheelJoints[i].setParamHiStop(0);
            }
        }
        ArrayList<DBody> bodies = new ArrayList<>();
        bodies.add(body);
        for(int i = 0; i < wheelBodies.length; i++){
            bodies.add(wheelBodies[i]);
        }
        collisionBodies.put(entity, bodies.toArray(new DBody[0]));
    }

    private List<RenderwareBinaryStream.Frame> getWheelFrames(VehicleModel model){
        List<RenderwareBinaryStream.Frame> dummyFrames = model.getDummyFrames();
        List<RenderwareBinaryStream.Frame> frames = new ArrayList<>();
        for(int i = 0; i < dummyFrames.size(); i++){
            if(model.getFrameName(dummyFrames.get(i)).startsWith("wheel_")){
                frames.add(dummyFrames.get(i));
            }
        }
        return frames;
    }

    public void pollQueue(){
        Runnable command;
        while ((command = executionQueue.poll()) != null) command.run();
    }

    // TODO: shutdown thread when error occurs
    @Override
    public void run() {
        try{
            handleCollisions();
            world.quickStep(1d / 60);
            contactGroup.empty();
            pollQueue();
            updateEntities();
            removeDeadEntities();
        }
        catch (Exception e){
            logger.error("Exception in physics thread: {}", e.getMessage(), e);
        }
    }

    private void handleCollisions(){
        space.collide(space, nearCallback);
    }

    private void updateEntities(){
        for (Map.Entry<CarEntity, DBody[]> entry : collisionBodies.entrySet()) {
            TickScheduler.CLIENT_RENDER_START.immediate(() -> {
                CarEntity entity = entry.getKey();
                DBody body = entry.getValue()[0];
                if(entity.isAlive()){
                    entity.setPosition(body.getPosition().get(0), body.getPosition().get(1), body.getPosition().get(2));

                    DMatrix3C rotationMatrix = body.getRotation();
                    entity.setRotationMatrix(rotationMatrix.toFloatArray());

                    List<float[]> wheelRotations = new ArrayList<>();
                    List<DVector3C> relativeWheelPosisions = new ArrayList<>();
                    for(int i = 1; i < entry.getValue().length; i++){
                        wheelRotations.add(entry.getValue()[i].getRotation().toFloatArray());
                        DVector3C vector = entry.getValue()[i].getPosition();
                        relativeWheelPosisions.add(new DVector3(vector.get0() - entity.x, vector.get1() - entity.y, vector.get2() - entity.z));
                    }

                    entity.setRelativeWheelPositions(relativeWheelPosisions);
                    entity.setWheelRotations(wheelRotations);
                }
                else {
                    removalQueue.add(entity);
                }
            });
        }
    }

    private void removeDeadEntities(){
        for(CarEntity entity : removalQueue){
            DBody body = collisionBodies.get(entity)[0];
            collisionBodies.remove(entity);
            body.destroy();
        }
    }

    private final DGeom.DNearCallback nearCallback = this::nearCallback;
    private void nearCallback (Object data, DGeom o1, DGeom o2)
    {
        int i,n;
        final int N = 10;

        if (o1 == null || o2 == null || OdeHelper.areConnected(o1.getBody(), o2.getBody())) {
            return; // Skip collision if bodies are connected by any joint
        }

        DContactBuffer contacts = new DContactBuffer(N);
        n = OdeHelper.collide (o1,o2,N,contacts.getGeomBuffer());
        if (n > 0) {
            for (i=0; i<n; i++) {
                DContact contact = contacts.get(i);
                contact.surface.mode = OdeConstants.dContactSlip1 | OdeConstants.dContactSlip2 |
                        OdeConstants.dContactSoftERP | OdeConstants.dContactSoftCFM | OdeConstants.dContactApprox1;
                contact.surface.mu = OdeConstants.dInfinity;
                contact.surface.slip1 = 0.001;
                contact.surface.slip2 = 0.001;
                contact.surface.soft_erp = 0.1;
                contact.surface.soft_cfm = 0;
                DJoint c = OdeHelper.createContactJoint (world,contactGroup,contact);
                c.attach(
                        contact.geom.g1.getBody(),
                        contact.geom.g2.getBody());
            }
        }
    }
}
