package ralf2oo2.carmod;

import com.matthewperiut.retrocommands.api.CommandRegistry;
import net.fabricmc.loader.api.FabricLoader;
import org.ode4j.ode.*;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;
import ralf2oo2.carmod.command.DebugCommand;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Carmod {
    public static PhysicsEngine physicsEngine;
    public Carmod(){
        physicsEngine = new PhysicsEngine();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(physicsEngine, 0, 1000 / 60, TimeUnit.MILLISECONDS);
        CommandRegistry.add(new DebugCommand());
    }
    public void demo(){
//        OdeHelper.initODE2(0);
//        world = OdeHelper.createWorld();
//        world.setGravity (0,-0.5,0);
//
//        space = OdeHelper.createSimpleSpace();
//
//        DBody body = OdeHelper.createBody(world);
//
//        DGeom geom = OdeHelper.createBox(space, 1, 1, 1);
//        geom.setPosition(0, 100, 0);
//        geom.setBody(body);
    }
}
