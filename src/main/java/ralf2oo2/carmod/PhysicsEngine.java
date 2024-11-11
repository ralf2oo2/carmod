package ralf2oo2.carmod;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.modificationstation.stationapi.api.tick.TickScheduler;
import net.modificationstation.stationapi.api.util.math.StationBlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.util.vector.Vector3f;
import org.ode4j.math.*;
import org.ode4j.ode.*;
import ralf2oo2.carmod.physics.Car;
import ralf2oo2.carmod.physics.PhysicsObject;
import ralf2oo2.carmod.util.RenderwareBinaryStream;
import ralf2oo2.carmod.entity.CarEntity;
import ralf2oo2.carmod.registry.VehicleRegistry;
import ralf2oo2.carmod.util.raycast.RaycastResult;
import ralf2oo2.carmod.vehicle.Vehicle;
import ralf2oo2.carmod.vehicle.VehicleModel;

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
    private final Map<CarEntity, PhysicsObject> physicsObjects = new HashMap<>();
    private final Map<CarEntity, Map<BlockPos, DGeom>> entityWorldCollision = new HashMap<>();
    private final List<CarEntity> removalQueue = new ArrayList<>();
    private long lastFrameTime = System.nanoTime();
    public static List<DVector3C> hitPoints = new ArrayList<>();
    public DGeom ray;
    public final Queue<Runnable> executionQueue = new ConcurrentLinkedQueue<>();
    public static float frameDelta = 0f;

    public PhysicsEngine(){
        OdeHelper.initODE2(0);
        world = OdeHelper.createWorld();
        world.setGravity (0,-9.81,0);
        contactGroup = OdeHelper.createJointGroup();
        space = OdeHelper.createSimpleSpace();
        world.setContactSurfaceLayer (0.001);
        //OdeHelper.createPlane(space, 0, 1, 0, 64);
    }
    public Optional<CarEntity> getEntityByBody(DBody body){
        for (Map.Entry<CarEntity, PhysicsObject> entry : physicsObjects.entrySet()) {
            if(entry.getValue().bodies.contains(body)){
                return Optional.ofNullable(entry.getKey());
            }
        }
        return Optional.empty();
    }

    public PhysicsObject getPhysicsObject(CarEntity carEntity){
        return physicsObjects.get(carEntity);
    }
    public Map<CarEntity, PhysicsObject> getPhysicsObjects(){
        return physicsObjects;
    }

    public void registerEntity(CarEntity entity){
        if(physicsObjects.containsKey(entity)) return;
        if(entity.carName == null) return;
        Optional<Vehicle> vehicle = VehicleRegistry.getVehicle(entity.carName);
        if(!vehicle.isPresent()) return;
        physicsObjects.put(entity, new Car(entity, world, space, vehicle.get()));
        entityWorldCollision.put(entity, new HashMap<>());
    }

    public void controlVehicle(CarEntity entity, float moveForwards, float moveSideways){
        PhysicsObject physicsObject = getPhysicsObject(entity);
        if(!(physicsObject instanceof Car)) return;
        Car car = (Car)physicsObject;
        for(int i = 0; i < car.joints.size(); i++){
            if(car.joints.get(i).getData().equals("b")){
                ((DHinge2Joint)car.joints.get(i)).setParamVel2(10.0 * moveForwards);
            }
            else{
                double targetSteeringAngle = Math.toRadians(45 * -moveSideways);
                double currentSteeringAngle = ((DHinge2Joint)car.joints.get(i)).getAngle1();

                double angleError = targetSteeringAngle - currentSteeringAngle;
                double gain = 1.0f;
                double maxSteeringForce = 10.0;
                double steeringVelocity = angleError * gain;
                ((DHinge2Joint)car.joints.get(i)).setParamVel(steeringVelocity);
                ((DHinge2Joint)car.joints.get(i)).setParamFMax(maxSteeringForce);
            }
        }
    }
    public void applyForceAtPosition(CarEntity carEntity, int bodyIndex, DVector3C forcePosition, DVector3C forceNormal, double force){
        if(bodyIndex < 0) return;
        PhysicsObject physicsObject = getPhysicsObject(carEntity);
        if(bodyIndex > physicsObject.bodies.size() - 1) return;
        DVector3C forceVector = forceNormal.reScale(force);
        forceVector = new DVector3(-forceVector.get0(), -forceVector.get1(), -forceVector.get2());
        physicsObject.bodies.get(bodyIndex).addForceAtPos(forceVector, forcePosition);
    }

    public Optional<RaycastResult> rayCast(PlayerEntity player, float length){
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

        RaycastCallback callback = new RaycastCallback(ray, this);
        space.collide(space, callback);
        this.ray = ray;
        ray.destroy();
        return Optional.ofNullable(callback.getResult());
    }

    public void pollQueue(){
        Runnable command;
        while ((command = executionQueue.poll()) != null) command.run();
    }
    private void updateFrameDelta(){
        float physicsInterval = 1.0f / 60.0f;
        long currentFrameTime = System.nanoTime();
        float elapsedTime = (currentFrameTime - lastFrameTime) / 1_000_000_000.0f;

        PhysicsEngine.frameDelta = java.lang.Math.min(elapsedTime/ physicsInterval, 1.0f);
        lastFrameTime = currentFrameTime;
    }

    // TODO: shutdown thread when error occurs
    @Override
    public void run() {
        try{
            updateFrameDelta();
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
        for (Map.Entry<CarEntity, PhysicsObject> entry : physicsObjects.entrySet()) {
            entry.getValue().update();
        }
    }

    private void updateWorldCollision(){
        Minecraft minecraft = CarmodClient.getMc();
        for (Map.Entry<CarEntity, PhysicsObject> entry : physicsObjects.entrySet()) {
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
        }
    }

    // TODO: implement this
    private void removeDeadEntities(){
//        for(CarEntity entity : removalQueue){
//            DBody body = collisionBodies.get(entity)[0];
//            collisionBodies.remove(entity);
//            body.destroy();
//        }
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
    PhysicsEngine physicsEngine;
    private RaycastResult raycastResult;
    public RaycastCallback(DGeom ray, PhysicsEngine physicsEngine){
        this.ray = ray;
        this.physicsEngine = physicsEngine;
    }
    @Override
    public void call(Object data, DGeom o1, DGeom o2) {
        if (o1 == ray || o2 == ray) {
            if(o1.getBody() != null || o2.getBody() != null){
                DContactGeomBuffer contact = new DContactGeomBuffer(1);
                if (OdeHelper.collide(o1, o2, 1, contact) > 0) {
                    PhysicsEngine.hitPoints.add(contact.get(0).pos);
                    DGeom target = null;
                    if(o1.getBody() == null) target = o2;
                    if(o2.getBody() == null) target = o1;
                    Optional<CarEntity> carEntity = Carmod.physicsEngine.getEntityByBody(target.getBody());
                    if(carEntity.isPresent()){
                        Optional<PhysicsObject> physicsObject = Optional.of(physicsEngine.getPhysicsObject(carEntity.get()));
                        raycastResult = new RaycastResult(
                                contact.get(0).pos,
                                contact.get(0).normal,
                                carEntity.get(),
                                physicsObject.isPresent() ? physicsObject.get().bodies.indexOf(target.getBody()) : -1
                        );
                    }
                }
            }
        }
    }
    public RaycastResult getResult(){
        return raycastResult;
    }
}
