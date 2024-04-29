package ralf2oo2.carmod.item;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.template.item.TemplateItem;
import net.modificationstation.stationapi.api.util.Identifier;
import org.checkerframework.checker.units.qual.C;
import ralf2oo2.carmod.entity.CarEntity;

public class ItemCarSpawner extends TemplateItem {
    public ItemCarSpawner(Identifier identifier) {
        super(identifier);
    }

    @Override
    public boolean useOnBlock(ItemStack stack, PlayerEntity user, World world, int x, int y, int z, int side) {
        CarEntity carEntity = new CarEntity(world);
        carEntity.x = x;
        carEntity.y = y + 1;
        carEntity.z = z;
        carEntity.carPath = FabricLoader.getInstance().getConfigDir() + "/test.dff";
        world.method_210(carEntity);
        return true;
    }
}
