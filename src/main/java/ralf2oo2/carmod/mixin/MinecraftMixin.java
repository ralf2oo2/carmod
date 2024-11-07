package ralf2oo2.carmod.mixin;

import net.minecraft.client.InteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ralf2oo2.carmod.Carmod;
import ralf2oo2.carmod.registry.ItemRegistry;
import ralf2oo2.carmod.util.raycast.RaycastResult;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow private int attackCooldown;
    @Shadow public ClientPlayerEntity player;
    @Shadow public InteractionManager interactionManager;

    @Inject(at = @At(value = "HEAD"), method = "handleMouseClick", cancellable = true)
    private void carmod_handleMouseClick(int mouseButton, CallbackInfo ci) {
        if (mouseButton != 0 || this.attackCooldown <= 0) {
            System.out.println("Clicking " + mouseButton);

            CompletableFuture<Optional<RaycastResult>> raycastResult = new CompletableFuture<>();

            Carmod.physicsEngine.executionQueue.add(() -> {
               Optional<RaycastResult> result = Carmod.physicsEngine.rayCast(player, interactionManager.getReachDistance());
                raycastResult.complete(result);
            });

            Optional<RaycastResult> result;
            try {
                result = raycastResult.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                result = Optional.empty();
            }
            if(result.isPresent()){
                RaycastResult raycastResult1 = result.get();
                System.out.println("Hit a vehicle");
                if(mouseButton == 1){
                    if(player.inventory.getSelectedItem() != null && player.inventory.getSelectedItem().itemId == ItemRegistry.pushStick.id){
                        player.swingHand();
                        Carmod.physicsEngine.executionQueue.add(() -> {
                            Carmod.physicsEngine.applyForceAtPosition(raycastResult1.entity, raycastResult1.bodyIndex, raycastResult1.hitPosition, raycastResult1.hitNormal, 1000f);
                        });
                    }
                    else{
                        player.setVehicle(raycastResult1.entity);
                    }
                }
                ci.cancel();
            }
        }
    }
}
