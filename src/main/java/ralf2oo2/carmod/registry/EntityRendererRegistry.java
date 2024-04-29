package ralf2oo2.carmod.registry;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.client.event.render.entity.EntityRendererRegisterEvent;
import net.modificationstation.stationapi.api.event.entity.EntityRegister;
import ralf2oo2.carmod.client.render.CarEntityRenderer;
import ralf2oo2.carmod.entity.CarEntity;

public class EntityRendererRegistry {
    @EventListener
    public void registerEntities(EntityRendererRegisterEvent event){
        event.renderers.put(CarEntity.class, new CarEntityRenderer());
    }
}
