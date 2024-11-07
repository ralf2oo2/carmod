package ralf2oo2.carmod.registry;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.item.Item;
import net.modificationstation.stationapi.api.event.registry.ItemRegistryEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import net.modificationstation.stationapi.api.template.item.TemplateItem;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.Namespace;
import net.modificationstation.stationapi.api.util.Null;
import ralf2oo2.carmod.item.ItemCarSpawner;

public class ItemRegistry {
    public static Item carSpawner;
    public static Item pushStick;

    @Entrypoint.Namespace
    public static final Namespace NAMESPACE = Null.get();

    @EventListener
    public void registerItems(ItemRegistryEvent event) {
        carSpawner = new ItemCarSpawner(Identifier.of(NAMESPACE, "carspawner")).setTranslationKey(NAMESPACE, "carspawner");
        pushStick = new TemplateItem(Identifier.of(NAMESPACE, "push_stick")).setTranslationKey(NAMESPACE, "push_stick");

        pushStick.setTexturePosition(5, 3);
    }
}
