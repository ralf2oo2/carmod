package ralf2oo2.carmod.physics;

import net.modificationstation.stationapi.api.tick.TickScheduler;
import org.lwjgl.util.vector.Vector3f;
import org.ode4j.math.DMatrix3C;
import org.ode4j.math.DQuaternion;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.*;
import ralf2oo2.carmod.entity.CarEntity;
import ralf2oo2.carmod.util.RenderwareBinaryStream;
import ralf2oo2.carmod.vehicle.Vehicle;
import ralf2oo2.carmod.vehicle.VehicleModel;

import java.util.ArrayList;
import java.util.List;

public class Car extends PhysicsObject{
    public final List<DJoint> joints = new ArrayList<>();
    public Vehicle vehicle;
    public Car(CarEntity carEntity, DWorld world, DSpace space, Vehicle vehicle) {
        super(carEntity, world, space);
        this.vehicle = vehicle;
        DBody body = OdeHelper.createBody(world);
        body.setPosition(carEntity.x, carEntity.y, carEntity.z);
        bodies.add(body);
        setupBodyCollisions(body);
        setupWheels(body);
    }

    @Override
    public void update() {
        TickScheduler.CLIENT_RENDER_START.immediate(() -> {
            DBody carBody = bodies.get(0);
            if(carEntity.isAlive()){
                DMatrix3C rotationMatrix = carBody.getRotation();

                List<float[]> wheelRotations = new ArrayList<>();
                List<DVector3C> wheelPosisions = new ArrayList<>();
                for(int i = 1; i < bodies.size(); i++){
                    wheelRotations.add(bodies.get(i).getRotation().toFloatArray());
                    DVector3C vector = bodies.get(i).getPosition();
                    wheelPosisions.add(vector);
                }

                carEntity.setPosition(carBody.getPosition().get(0), carBody.getPosition().get(1), carBody.getPosition().get(2));
                carEntity.setRotationMatrix(rotationMatrix.toFloatArray());
                carEntity.setWheelPositions(wheelPosisions);
                carEntity.setWheelRotations(wheelRotations);
            }
            else {
                // TODO: remove dead entities
            }
        });
    }

    private void setupBodyCollisions(DBody body){
        RenderwareBinaryStream.Col collisions = vehicle.vehicleCollisions.collisions;
        for(int i = 0; i < collisions.spheres().size(); i++){
            RenderwareBinaryStream.ColSphere sphere = collisions.spheres().get(i);
            DGeom geom = OdeHelper.createSphere(space, sphere.radius());
            geom.setBody(body);
            geom.setOffsetPosition(sphere.center().x(), sphere.center().z(), sphere.center().y());
        }
        DGeom meshGeom = OdeHelper.createTriMesh(space, vehicle.vehicleCollisions.meshCollider);
        meshGeom.setBody(body);
        DMass mass = OdeHelper.createMass();
        RenderwareBinaryStream.ColBounds bounds = collisions.header().bounds();
        mass.setBoxTotal(vehicle.vehicleHandling.mass, bounds.max().z() - bounds.min().z(), bounds.max().x() - bounds.min().x(), bounds.max().y() - bounds.min().y());
        body.setMass(mass);
    }

    private void setupWheels(DBody body){
        List<RenderwareBinaryStream.Frame> wheelFrames = getWheelFrames(vehicle.vehicleModel);

        DMass wheelMass = OdeHelper.createMass();
        wheelMass.setCylinder(1, 1, 0.7, 0.3);
        wheelMass.adjust(vehicle.vehicleHandling.mass / 4);

        for(int i = 0; i < wheelFrames.size(); i++){
            boolean front = vehicle.vehicleModel.getFrameName(wheelFrames.get(i)).substring(7).startsWith("f");
            DBody wheelBody = OdeHelper.createBody(world);
            DQuaternion q = new DQuaternion();
            OdeMath.dQFromAxisAndAngle (q,0,1,0, java.lang.Math.PI*0.5);
            wheelBody.setQuaternion(q);
            wheelBody.setMass(wheelMass);

            DGeom cylinder = OdeHelper.createCylinder(space, front ? vehicle.vehicleData.frontWheelsSize / 2f : vehicle.vehicleData.rearWheelsSize / 2f, 0.2);
            cylinder.setBody(wheelBody);

            Vector3f wheelOffset = vehicle.vehicleModel.getFrameOffset(wheelFrames.get(i));

            float rotatedX = -wheelOffset.x;
            float rotatedZ = -wheelOffset.z;

            wheelBody.setPosition(rotatedX + body.getPosition().get0(), (wheelOffset.y + vehicle.vehicleHandling.suspensionLowerLimit) + body.getPosition().get1(), rotatedZ + body.getPosition().get2());

            DHinge2Joint hingeJoint = OdeHelper.createHinge2Joint(world, null);
            hingeJoint.attach(body, wheelBody);
            if(vehicle.vehicleModel.getFrameName(wheelFrames.get(i)).substring(6).startsWith("l")){
                carEntity.wheelSides.add("left");
            } else {
                carEntity.wheelSides.add("right");
            }

            final DVector3C a = wheelBody.getPosition();
            hingeJoint.setAnchor(a);
            hingeJoint.setAxes(0,1,0 ,-1, 0, 0);
            hingeJoint.setParamFMax2(0f);
            hingeJoint.setParamFMax(vehicle.vehicleHandling.turnMass);

            hingeJoint.setParamSuspensionERP(1.8f);
            hingeJoint.setParamSuspensionCFM(0.001f);

            if(!front){
                hingeJoint.setParamFMax2(1000f);
                hingeJoint.setParamLoStop(0);
                hingeJoint.setParamHiStop(0);
                hingeJoint.setData("b");
            }
            else {
                hingeJoint.setData("f");
                hingeJoint.setParamLoStop(Math.toRadians(-vehicle.vehicleHandling.steeringLock));
                hingeJoint.setParamHiStop(Math.toRadians(vehicle.vehicleHandling.steeringLock));
            }
            bodies.add(wheelBody);
            joints.add(hingeJoint);
        }
    }

    private List<RenderwareBinaryStream.Frame> getWheelFrames(VehicleModel model){
        List<RenderwareBinaryStream.Frame> dummyFrames = model.getDummyFrames();
        List<RenderwareBinaryStream.Frame> frames = new ArrayList<>();
        for(int i = 0; i < dummyFrames.size(); i++){
            if(model.getFrameName(dummyFrames.get(i)).startsWith("wheel_")){
                frames.add(dummyFrames.get(i));
            }
        }
        return frames;
    }
}
