package ralf2oo2.carmod.client.screen;

import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import ralf2oo2.carmod.client.render.TxdTexture;
import ralf2oo2.carmod.registry.VehicleTextureRegistry;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TestScreen extends Screen {
    TxdTexture selectedTexture;
    int selectedTextureIndex;
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if(VehicleTextureRegistry.textureCount() == 0){
            return;
        }
        selectedTexture = VehicleTextureRegistry.getVehicleTextures().get(selectedTextureIndex);
        String textureFormat = new String(ByteBuffer.allocate(4).putInt(selectedTexture.textureFormat).array(), StandardCharsets.UTF_8);
        System.out.println(textureFormat);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glBindTexture(3553, selectedTexture.textureId);
        drawTexture(0, 0, 0, 0, selectedTexture.width, selectedTexture.height);
    }

    @Override
    protected void keyPressed(char character, int keyCode) {
        if(Keyboard.isKeyDown(Keyboard.KEY_O)){
            selectedTextureIndex++;
            if(selectedTextureIndex == VehicleTextureRegistry.textureCount()){
                selectedTextureIndex = 0;
            }
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_P)){
            selectedTextureIndex--;
            if(selectedTextureIndex == -1){
                selectedTextureIndex = VehicleTextureRegistry.textureCount() - 1;
            }
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
            minecraft.setScreen(null);
        }
    }
}
