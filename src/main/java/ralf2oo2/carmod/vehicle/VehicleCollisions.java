package ralf2oo2.carmod.vehicle;

import org.ode4j.ode.DWorld;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;

public class VehicleCollisions {
    private RenderwareBinaryStream binaryStream;
    public RenderwareBinaryStream.Col collisions;
    public VehicleCollisions(RenderwareBinaryStream binaryStream){
        this.binaryStream = binaryStream;
        RenderwareBinaryStream.StructExtension colextension = (RenderwareBinaryStream.StructExtension) ((RenderwareBinaryStream.ListWithHeader) binaryStream.body()).entries().get(((RenderwareBinaryStream.ListWithHeader) binaryStream.body()).entries().size() - 1).body();
        this.collisions = ((RenderwareBinaryStream.CollisionExtension)colextension.extension()).col();
    }

    public void InstanceiateCollisions(DWorld world){

    }
}
