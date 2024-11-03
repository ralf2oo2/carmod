package ralf2oo2.carmod.listener;

import net.fabricmc.loader.api.FabricLoader;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.modificationstation.stationapi.api.client.event.keyboard.KeyStateChangedEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.ode4j.math.DVector3;
import org.ode4j.ode.DGeom;
import ralf2oo2.carmod.Carmod;
import ralf2oo2.carmod.CarmodClient;
import ralf2oo2.carmod.client.render.DebugRenderer;
import ralf2oo2.carmod.client.screen.TestScreen;

import java.util.Iterator;

public class KeyPressedListener {
    @EventListener
    public void keyPressed(KeyStateChangedEvent event) {
        Minecraft mc = Minecraft.class.cast(FabricLoader.getInstance().getGameInstance());
        if(event.environment == KeyStateChangedEvent.Environment.IN_GAME && Keyboard.getEventKey() == Keyboard.KEY_M){
            mc.setScreen(new TestScreen());
        }
        if(event.environment == KeyStateChangedEvent.Environment.IN_GAME && Keyboard.isKeyDown(Keyboard.KEY_H)){
            DebugRenderer.active = !DebugRenderer.active;
            System.out.println("Activated debug renderer");
        }
        if(event.environment == KeyStateChangedEvent.Environment.IN_GAME && Keyboard.getEventKey() == Keyboard.KEY_P){
            Iterator<DGeom> iterator = Carmod.space.getGeoms().iterator(); // Replace GeometryType with the actual type

            while (iterator.hasNext()) {
                DGeom geom = iterator.next();
                PlayerEntity player = CarmodClient.getMc().player;
                geom.setPosition(player.x, player.y + 20, player.z);
                System.out.println(geom); // Or whatever processing you need
            }
        }
        if(event.environment == KeyStateChangedEvent.Environment.IN_GAME && Keyboard.getEventKey() == Keyboard.KEY_O){
            Carmod.world.quickStep(1d);
        }
    }
}
