package ralf2oo2.carmod;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public class CarmodClient {
    public static Minecraft getMc(){
        return (Minecraft)FabricLoader.getInstance().getGameInstance();
    }
}
