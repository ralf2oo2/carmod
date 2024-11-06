package ralf2oo2.carmod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.lwjgl.util.vector.Matrix4f;
import org.ode4j.math.DVector3C;
import ralf2oo2.carmod.Carmod;
import ralf2oo2.carmod.Utils.Math;

import java.util.ArrayList;
import java.util.List;

public class CarEntity extends Entity {
    public List<DVector3C> wheelPositions;
    public List<Matrix4f> wheelRotations;
    public List<String> wheelSides = new ArrayList<>();
    public Matrix4f rotationMatrix;
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
    public Box getCollisionAgainstShape(Entity other) {
        System.out.println("collision shape");
        return super.getCollisionAgainstShape(other);
    }

    public void setWheelPositions(List<DVector3C> wheelPositions){
        this.wheelPositions = wheelPositions;
    }

    public void setWheelRotations(List<float[]> wheelRotations){
        this.wheelRotations = new ArrayList<>();
        for(int i = 0; i < wheelRotations.size(); i++){
            this.wheelRotations.add(Math.convertMatrix(wheelRotations.get(i)));
        }
    }

    public void setRotationMatrix(float[] rotationMatrix){
        this.rotationMatrix = Math.convertMatrix(rotationMatrix);
    }

    @Override
    public void tick() {
        world.addParticle("note", (double)x, (double)y, (double)z, 24.0, 0.0, 0.0);
        if(carName == null){
            dead = true;
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
