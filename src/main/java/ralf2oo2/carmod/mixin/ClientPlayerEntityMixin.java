package ralf2oo2.carmod.mixin;

import net.minecraft.client.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ralf2oo2.carmod.Carmod;
import ralf2oo2.carmod.entity.CarEntity;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin extends LivingEntity {
    @Shadow public Input input;
    public ClientPlayerEntityMixin(){
        super(null);
    }
    public ClientPlayerEntityMixin(World world) {
        super(world);
    }

    @Inject(at = @At(value = "HEAD"), method = "tickMovement")
    private void carmod_handleMouseClick(CallbackInfo ci) {
        if(vehicle instanceof CarEntity){
            Carmod.physicsEngine.executionQueue.add(() -> {
                Carmod.physicsEngine.controlVehicle((CarEntity)vehicle, input.movementForward, input.movementSideways);
            });
        }
    }

    @Override
    protected void initDataTracker() {

    }
}
