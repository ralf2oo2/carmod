package ralf2oo2.carmod;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.modificationstation.stationapi.api.tick.TickScheduler;
import net.modificationstation.stationapi.api.util.math.StationBlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.ode4j.math.*;
import org.ode4j.ode.*;
import ralf2oo2.carmod.Utils.Math;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;
import ralf2oo2.carmod.entity.CarEntity;
import ralf2oo2.carmod.registry.VehicleRegistry;
import ralf2oo2.carmod.vehicle.Vehicle;
import ralf2oo2.carmod.vehicle.VehicleModel;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public class PhysicsEngine implements Runnable{
    private static final Logger logger = LogManager.getLogger(PhysicsEngine.class);
    private static final int MAX_CONTACTS = 10;
    private static final int BOUNDING_PADDING = 2;
    private static final DVector3C yunit = new DVector3(0, 1, 0);
    private static final DVector3C zunit = new DVector3(-1, 0, 0);
    private final DWorld world;
    private final DSpace space;
    private final DJointGroup contactGroup;
    private final Map<CarEntity, DBody[]> collisionBodies = new HashMap<>();
    private final Map<CarEntity, Map<BlockPos, DGeom>> entityWorldCollision = new HashMap<>();
    private final List<CarEntity> removalQueue = new ArrayList<>();
    public static List<DVector3C> hitPoints = new ArrayList<>();
    public DGeom ray;
    public final Queue<Runnable> executionQueue = new ConcurrentLinkedQueue<>();

    public PhysicsEngine(){
        OdeHelper.initODE2(0);
        world = OdeHelper.createWorld();
        world.setGravity (0,-9.81,0);
        contactGroup = OdeHelper.createJointGroup();
        space = OdeHelper.createSimpleSpace();
        world.setContactSurfaceLayer (0.001);
        //OdeHelper.createPlane(space, 0, 1, 0, 64);
    }

    public Optional<DBody[]> getEntityBodies(CarEntity entity){
        if(!collisionBodies.containsKey(entity)) return Optional.empty();
        return Optional.ofNullable(collisionBodies.get(entity));
    }
    public Map<CarEntity, DBody[]> getCollisionBodies(){
        return this.collisionBodies;
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
        mass.setBoxTotal(10f, bounds.max().z() - bounds.min().z(), bounds.max().x() - bounds.min().x(), bounds.max().y() - bounds.min().y());
        body.setMass(mass);

        List<RenderwareBinaryStream.Frame> wheelFrames = getWheelFrames(vehicle.get().vehicleModel);

        DBody[] wheelBodies = new DBody[wheelFrames.size()];
        DHinge2Joint[] wheelJoints = new DHinge2Joint[wheelFrames.size()];

        DMass wheelMass = OdeHelper.createMass();
        wheelMass.setCylinder(1, 1, 0.7, 0.3);
        wheelMass.adjust(1f);

        for(int i = 0; i < wheelFrames.size(); i++){
            wheelBodies[i] = OdeHelper.createBody(world);
            DQuaternion q = new DQuaternion();
            OdeMath.dQFromAxisAndAngle (q,0,1,0, java.lang.Math.PI*0.5);
            wheelBodies[i].setQuaternion(q);
            wheelBodies[i].setMass(wheelMass);

            DGeom cylinder = OdeHelper.createCylinder(space, 0.3d, 0.2);
            cylinder.setBody(wheelBodies[i]);

            Vector3f wheelOffset = vehicle.get().vehicleModel.getFrameOffset(wheelFrames.get(i));

            float rotatedX = -wheelOffset.x;  // Inverting X
            float rotatedZ = -wheelOffset.z;

            float suspensionHeight = 0.3f;
            wheelBodies[i].setPosition(rotatedX + body.getPosition().get0(), (wheelOffset.y - suspensionHeight) + body.getPosition().get1(), rotatedZ + body.getPosition().get2());

            wheelJoints[i] = OdeHelper.createHinge2Joint(world, null);
            wheelJoints[i].attach(body, wheelBodies[i]);
            if(vehicle.get().vehicleModel.getFrameName(wheelFrames.get(i)).substring(6).startsWith("l")){
                entity.wheelSides.add("left");
            } else {
                entity.wheelSides.add("right");
            }

            final DVector3C a = wheelBodies[i].getPosition();
            DHinge2Joint h2 = wheelJoints[i];
            h2.setAnchor(a);
            h2.setAxes(0,1,0 ,-1, 0, 0);
            h2.setParamVel2(0);
            h2.setParamFMax2(25f);
            h2.setParamFMax(25f);

            wheelJoints[i].setParamSuspensionERP(0.8f);
            wheelJoints[i].setParamSuspensionCFM(0.14f);

            wheelJoints[i].setParamVel2(5.0);

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
        entityWorldCollision.put(entity, new HashMap<>());
    }

    public Optional<CarEntity> rayCast(PlayerEntity player, float length){
        hitPoints.clear();
        DRay ray = OdeHelper.createRay(space, length);
        DVector3C lookPos = new DVector3(player.x, player.y, player.z);
        Vec3d lookVector = player.getLookVector();
        Vec3d up = Vec3d.create(0, 1, 0);
        Vec3d right = up.crossProduct(lookVector).normalize();
        Vec3d recalculatedUp = lookVector.crossProduct(right).normalize();

        DMatrix3 rotationMatrix = new DMatrix3(
                right.x, recalculatedUp.x, lookVector.x,  // First column
                right.y, recalculatedUp.y, lookVector.y,  // Second column
                right.z, recalculatedUp.z, lookVector.z
        );

        ray.setPosition(lookPos);
        ray.setRotation(rotationMatrix);

        RaycastCallback callback = new RaycastCallback(ray);
        space.collide(space, callback);
        this.ray = ray;
        ray.destroy();
        return Optional.empty();
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
            updateWorldCollision();
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

                    List<DVector3C> wheelPosisions = new ArrayList<>();
                    for(int i = 1; i < entry.getValue().length; i++){
                        wheelRotations.add(entry.getValue()[i].getRotation().toFloatArray());
                        DVector3C vector = entry.getValue()[i].getPosition();
                        wheelPosisions.add(vector);
                    }

                    entity.setWheelPositions(wheelPosisions);
                    entity.setWheelRotations(wheelRotations);
                }
                else {
                    removalQueue.add(entity);
                }
            });
        }
    }

    private void updateWorldCollision(){
        Minecraft minecraft = CarmodClient.getMc();
        for (Map.Entry<CarEntity, DBody[]> entry : collisionBodies.entrySet()) {
            CarEntity carEntity = entry.getKey();
            if(carEntity.prevX == carEntity.x) return;


            Map<BlockPos, DGeom> worldCollision = this.entityWorldCollision.get(entry.getKey());
            Vehicle vehicle = VehicleRegistry.getVehicle(carEntity.carName).get();
            RenderwareBinaryStream.ColBounds bounds = vehicle.vehicleCollisions.collisions.header().bounds();
            float width = bounds.max().y() - bounds.min().y();
            float height = bounds.max().z() - bounds.min().z();

            Box boundingBox = Box.create(carEntity.x - (width  * 0.5 + BOUNDING_PADDING),
                    carEntity.y - (height  * 0.5 + BOUNDING_PADDING),
                    carEntity.z - (width  * 0.5 + BOUNDING_PADDING),
                    carEntity.x + (width  * 0.5 + BOUNDING_PADDING),
                    carEntity.y + (height  * 0.5 + BOUNDING_PADDING),
                    carEntity.z + (width  * 0.5 + BOUNDING_PADDING));

            //BlockPos entityPos = new BlockPos((int)Math.floor(carEntity.x), (int)Math.floor(carEntity.y), (int)Math.floor(carEntity.z));
            Stream<BlockPos> blockStream = StationBlockPos.stream(boundingBox);

            blockStream.forEach(blockPos -> {
                if(!worldCollision.containsKey(blockPos.toImmutable())){
                    int id = minecraft.world.getBlockId(blockPos.x, blockPos.y, blockPos.z);
                    if(id > 0) {
                        DGeom block = OdeHelper.createBox(space, 1, 1, 1);
                        block.setPosition((double) blockPos.x + 0.5, (double) blockPos.y + 0.5, (double) blockPos.z + 0.5);
                        worldCollision.put(blockPos.toImmutable(), block);
                    }
                }
            });

            List<BlockPos> outOfRangeBlocks = new ArrayList<>();
            for (Map.Entry<BlockPos, DGeom> collisionEntry : worldCollision.entrySet()) {
                BlockPos currentBlockPos = collisionEntry.getKey();
                if(!boundingBox.contains(Vec3d.create(currentBlockPos.x, currentBlockPos.y, currentBlockPos.z))){
                    outOfRangeBlocks.add(currentBlockPos);
                    space.remove(collisionEntry.getValue());
                    collisionEntry.getValue().destroy();
                }
            }
            for(int i = 0; i < outOfRangeBlocks.size(); i++){
                worldCollision.remove(outOfRangeBlocks.get(i));
            }
//            TickScheduler.CLIENT_RENDER_START.immediate(() -> {
//            });
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

        if(o1.getBody() != null && o2.getBody() != null){
            if (o1 == null || o2 == null || OdeHelper.areConnected(o1.getBody(), o2.getBody())) {
                return; // Skip collision if bodies are connected by any joint
            }
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
class RaycastCallback implements DGeom.DNearCallback{
    DGeom ray;
    public RaycastCallback(DGeom ray){
        this.ray = ray;
    }
    @Override
    public void call(Object data, DGeom o1, DGeom o2) {
        if (o1 == ray || o2 == ray) {
            if(o1.getBody() != null || o2.getBody() != null){
                DContactGeomBuffer contact = new DContactGeomBuffer(1);
                if (OdeHelper.collide(o1, o2, 1, contact) > 0) {
                    System.out.println("Ray hit an object!");
                    PhysicsEngine.hitPoints.add(contact.get(0).pos);
                }
            }
        }
    }
}
