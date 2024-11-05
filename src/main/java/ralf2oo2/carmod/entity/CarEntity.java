package ralf2oo2.carmod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.util.vector.Vector3f;
import org.ode4j.math.DVector3C;
import ralf2oo2.carmod.Carmod;

import java.util.ArrayList;
import java.util.List;

public class CarEntity extends Entity {
    public List<DVector3C> relativeWheelPositions;
    public List<float[]> wheelRotations;
    public float[] rotationMatrix;
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
    public Box getCollisionAgainstShape(Entity other) {
        System.out.println("collision shape");
        return super.getCollisionAgainstShape(other);
    }


    public float[] convertRotationMatrix(float[] rotationMatrix) {
        float[] rotationMatrix4x4 = new float[16];

        rotationMatrix4x4[0] = rotationMatrix[0];
        rotationMatrix4x4[1] = rotationMatrix[3];
        rotationMatrix4x4[2] = rotationMatrix[6];

        rotationMatrix4x4[4] = rotationMatrix[1];
        rotationMatrix4x4[5] = rotationMatrix[4];
        rotationMatrix4x4[6] = rotationMatrix[7];

        rotationMatrix4x4[8] = rotationMatrix[2];
        rotationMatrix4x4[9] = rotationMatrix[5];
        rotationMatrix4x4[10] = rotationMatrix[8];

        rotationMatrix4x4[15] = 1.0f;

        return rotationMatrix4x4;
    }

    public void setRelativeWheelPositions(List<DVector3C> relativeWheelPositions){
        this.relativeWheelPositions = relativeWheelPositions;
    }

    public void setWheelRotations(List<float[]> wheelRotations){
        this.wheelRotations = new ArrayList<>();
        for(int i = 0; i < wheelRotations.size(); i++){
            this.wheelRotations.add(convertRotationMatrix(wheelRotations.get(i)));
        }
    }

    public void setRotationMatrix(float[] rotationMatrix){
        this.rotationMatrix = convertRotationMatrix(rotationMatrix);
    }

    public float[] getRelativeRotation(float[] M1, float[] M2) {

        float[] relativeRotation = new float[16];
        float[] M2T = new float[16];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                M2T[i * 4 + j] = M2[j * 4 + i];
            }
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                relativeRotation[i * 4 + j] = 0;
                for (int k = 0; k < 4; k++) {
                    relativeRotation[i * 4 + j] += M2T[i * 4 + k] * M1[k * 4 + j];
                }
            }
        }

        return relativeRotation;
    }

    @Override
    public void tick() {
        world.addParticle("note", (double)x, (double)y, (double)z, 24.0, 0.0, 0.0);
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
