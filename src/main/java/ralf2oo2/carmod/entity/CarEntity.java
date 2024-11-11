package ralf2oo2.carmod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.lwjgl.util.vector.Matrix4f;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import ralf2oo2.carmod.Carmod;
import ralf2oo2.carmod.registry.VehicleRegistry;
import ralf2oo2.carmod.util.Math;
import ralf2oo2.carmod.util.raycast.RaycastResult;
import ralf2oo2.carmod.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CarEntity extends Entity {
    public DVector3C prevPosition = new DVector3();
    public List<DVector3C> wheelPositions;
    public List<DVector3C> prevWheelPositions;
    public List<Matrix4f> wheelRotations;
    public List<Matrix4f> prevWheelRotations;
    public List<String> wheelSides = new ArrayList<>();
    public Matrix4f rotationMatrix = new Matrix4f();
    public Matrix4f prevRotationMatrix = new Matrix4f();
    public String carName;
    public CarEntity(World world) {
        super(world);
        this.setBoundingBoxSpacing(3, 3);
        this.renderDistanceMultiplier = 4;
        this.ignoreFrustumCull = true;
    }

    @Override
    protected void initDataTracker() {
        System.out.println("Init datatracker");
        Carmod.physicsEngine.executionQueue.add(() -> {
            Carmod.physicsEngine.registerEntity(this);
        });
    }

    @Override
    public boolean interact(PlayerEntity player) {
        System.out.println("Interacted");
        return super.interact(player);
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.prevPosition = new DVector3(this.x, this.y, this.z);
        super.setPosition(x, y, z);
    }

    @Override
    public Box getCollisionAgainstShape(Entity other) {
        System.out.println("collision shape");
        return super.getCollisionAgainstShape(other);
    }

    @Override
    public void updatePassengerPosition() {
        Optional<Vehicle> vehicle = VehicleRegistry.getVehicle(carName);
        if(vehicle.isPresent()){
            passenger.setPosition(x, y + 1, z);
        }
        else {
            super.updatePassengerPosition();
        }
    }

    public void setWheelPositions(List<DVector3C> wheelPositions){
        this.prevWheelPositions = this.wheelPositions;
        this.wheelPositions = wheelPositions;
    }

    public void setWheelRotations(List<float[]> wheelRotations){
        this.prevWheelRotations = this.wheelRotations;
        this.wheelRotations = new ArrayList<>();
        for(int i = 0; i < wheelRotations.size(); i++){
            this.wheelRotations.add(Math.convertMatrix(wheelRotations.get(i)));
        }
    }

    public void setRotationMatrix(float[] rotationMatrix){
        this.prevRotationMatrix = this.rotationMatrix;
        this.rotationMatrix = Math.convertMatrix(rotationMatrix);
    }

    @Override
    public void tick() {
        world.addParticle("note", x, y, z, 24.0, 0.0, 0.0);
        if(carName == null){
            dead = true;
        } else {
//            CompletableFuture<DVector3C> vehiclePositionFuture = new CompletableFuture<>();
//            CompletableFuture<float[]> vehicleRotationFuture = new CompletableFuture<>();
//            CompletableFuture<List<DVector3C>> wheelPositionsFuture = new CompletableFuture<>();
//            CompletableFuture<List<float[]>> wheelRotationsFuture = new CompletableFuture<>();
//
//            Carmod.physicsEngine.executionQueue.add(() -> {
//                vehiclePositionFuture.complete(Carmod.physicsEngine.getBodyPosition(this));
//                vehicleRotationFuture.complete(Carmod.physicsEngine.getBodyRotation(this));
//                wheelPositionsFuture.complete(Carmod.physicsEngine.getWheelPositions(this));
//                wheelRotationsFuture.complete(Carmod.physicsEngine.getWheelRotations(this));
//            });
//
//            try {
//                DVector3C vehiclePosition = vehiclePositionFuture.get();
//                float[] vehicleRotation = vehicleRotationFuture.get();
//                List<DVector3C> wheelPositions = wheelPositionsFuture.get();
//                List<float[]> wheelRotations = wheelRotationsFuture.get();
//
//                setPosition(vehiclePosition.get0(), vehiclePosition.get1(), vehiclePosition.get2());
//                setRotationMatrix(vehicleRotation);
//                setWheelPositions(wheelPositions);
//                setWheelRotations(wheelRotations);
//            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
        }
    }

//    @Override
//    public boolean (Vec3d arg) {
//        //System.out.println(arg.x + " " + arg.y + " " + arg.z);
//        return true;
//    }
//
//    @Override
//    public boolean method_1364(double d) {
//        return true;
//    }

    @Override
    protected void readNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {

    }
}
