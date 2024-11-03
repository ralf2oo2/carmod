package ralf2oo2.carmod.listener;

import net.fabricmc.loader.api.FabricLoader;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.client.Minecraft;
import net.modificationstation.stationapi.api.client.event.keyboard.KeyStateChangedEvent;
import org.lwjgl.input.Keyboard;
import ralf2oo2.carmod.client.render.DebugRenderer;
import ralf2oo2.carmod.client.screen.TestScreen;

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
    }
}
