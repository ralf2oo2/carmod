package ralf2oo2.carmod.item;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.template.item.TemplateItem;
import net.modificationstation.stationapi.api.util.Identifier;
import org.checkerframework.checker.units.qual.C;
import ralf2oo2.carmod.Carmod;
import ralf2oo2.carmod.CarmodClient;
import ralf2oo2.carmod.entity.CarEntity;
import ralf2oo2.carmod.gui.CarSpawnerScreen;

public class ItemCarSpawner extends TemplateItem {
    public ItemCarSpawner(Identifier identifier) {
        super(identifier);
    }

    @Override
    public boolean useOnBlock(ItemStack stack, PlayerEntity user, World world, int x, int y, int z, int side) {
        if(!stack.getStationNbt().contains("carName")) return false;
        CarEntity carEntity = new CarEntity(world);
        carEntity.x = x;
        carEntity.y = y + 5;
        carEntity.z = z;
        carEntity.carName = stack.getStationNbt().getString("carName");
        world.spawnEntity(carEntity);
        return true;
    }

    @Override
    public ItemStack use(ItemStack stack, World world, PlayerEntity user) {
        CarmodClient.getMc().setScreen(new CarSpawnerScreen(null, stack));
        return super.use(stack, world, user);
    }
}
