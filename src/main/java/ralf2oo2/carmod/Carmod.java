package ralf2oo2.carmod;

import net.fabricmc.loader.api.FabricLoader;
import org.ode4j.ode.*;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;

public class Carmod {
    private static DWorld world;
    private static DSpace space;
    public Carmod(){
        demo();
    }
    public void demo(){
        OdeHelper.initODE2(0);
        world = OdeHelper.createWorld();
        world.setGravity (0,0,-0.5);

        space = OdeHelper.createSimpleSpace();

        DBody body = OdeHelper.createBody(world);

        DGeom geom = OdeHelper.createBox(space, 1, 1, 1);
        geom.setBody(body);
    }
}
