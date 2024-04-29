package ralf2oo2.carmod.registry;

import net.mine_diver.unsafeevents.Event;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.event.entity.EntityRegister;
import ralf2oo2.carmod.client.render.CarEntityRenderer;
import ralf2oo2.carmod.entity.CarEntity;

public class EntityRegistry {
    @EventListener
    public void registerEntities(EntityRegister event){
        event.register(CarEntity.class, "car");
    }
}
