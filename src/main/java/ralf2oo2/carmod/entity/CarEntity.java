package ralf2oo2.carmod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class CarEntity extends Entity {
    public String carName;
    public CarEntity(World world) {
        super(world);
        this.setBoundingBoxSpacing(5, 5);
    }

    @Override
    protected void initDataTracker() {

    }

    @Override
    protected void readNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeNbt(NbtCompound nbt) {

    }

}
