package ralf2oo2.carmod.util.raycast;

import org.ode4j.math.DVector3C;
import ralf2oo2.carmod.entity.CarEntity;

public class RaycastResult {
    public DVector3C hitPosition;
    public DVector3C hitNormal;
    public CarEntity entity;
    public int bodyIndex;
    public RaycastResult(DVector3C hitPosition, DVector3C hitNormal, CarEntity entity, int bodyIndex){
        this.hitPosition = hitPosition;
        this.hitNormal = hitNormal;
        this.entity = entity;
        this.bodyIndex = bodyIndex;
    }
}
