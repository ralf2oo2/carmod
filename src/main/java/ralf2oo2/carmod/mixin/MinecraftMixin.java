package ralf2oo2.carmod.mixin;

import net.minecraft.client.InteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ralf2oo2.carmod.Carmod;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow private int attackCooldown;
    @Shadow public ClientPlayerEntity player;
    @Shadow public InteractionManager interactionManager;

    @Inject(at = @At(value = "HEAD"), method = "handleMouseClick", cancellable = true)
    private void carmod_handleMouseClick(int mouseButton, CallbackInfo ci) {
        if (mouseButton != 0 || this.attackCooldown <= 0) {
            System.out.println("Clicking " + mouseButton);
            Carmod.physicsEngine.executionQueue.add(() -> {
               Carmod.physicsEngine.rayCast(player, interactionManager.getReachDistance());
            });
        }
    }
}
