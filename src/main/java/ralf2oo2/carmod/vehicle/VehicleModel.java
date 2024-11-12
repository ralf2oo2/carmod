package ralf2oo2.carmod.vehicle;

import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import ralf2oo2.carmod.util.BinaryStreamHelpers;
import ralf2oo2.carmod.util.RenderwareBinaryStream;
import ralf2oo2.carmod.client.render.Geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehicleModel {
    private RenderwareBinaryStream binaryStream;
    private List<Geometry> geometryList = new ArrayList<>();
    private List<RenderwareBinaryStream> atomicList;
    private RenderwareBinaryStream.ListWithHeader frameList;
    private List<String> frameNames = new ArrayList<>();
    private List<String> modelBlacklist = new ArrayList<>();
    public VehicleModel(RenderwareBinaryStream binaryStream){
        this.binaryStream = binaryStream;

        loadData();
        for(int i = 0; i < frameNames.size(); i++){
            if(frameNames.get(i).endsWith("dam") || frameNames.get(i).endsWith("dummy") || frameNames.get(i).endsWith("lo")){
                modelBlacklist.add(frameNames.get(i));
            }
        }
        modelBlacklist.add("wheel");
    }

    public void loadData(){
        RenderwareBinaryStream.ListWithHeader geometryList = (RenderwareBinaryStream.ListWithHeader)((RenderwareBinaryStream.ListWithHeader) binaryStream.body()).entries().get(1).body();

        this.geometryList.clear();
        for(RenderwareBinaryStream geometryBinaryStream : geometryList.entries()){
            this.geometryList.add(new Geometry(geometryBinaryStream));
        }

        this.frameList = (RenderwareBinaryStream.ListWithHeader)((RenderwareBinaryStream.ListWithHeader) binaryStream.body()).entries().get(0).body();
        this.atomicList = ((RenderwareBinaryStream.ListWithHeader) binaryStream.body()).entries().stream().filter((entry) -> entry.code().name().equals("ATOMIC")).toList();

        RenderwareBinaryStream.ListWithHeader entries = (RenderwareBinaryStream.ListWithHeader)((RenderwareBinaryStream.ListWithHeader)((RenderwareBinaryStream.ListWithHeader) binaryStream.body()).entries().get(0).body());
        for(RenderwareBinaryStream stream : entries.entries()){
            RenderwareBinaryStream.StructExtension extension = (RenderwareBinaryStream.StructExtension) stream.body();
            RenderwareBinaryStream.FrameExtension frameExtension = (RenderwareBinaryStream.FrameExtension) extension.extension();
            frameNames.add(frameExtension.name());
            System.out.println("Added vehicle part: " + frameExtension.name());
        }
    }

    public Optional<Geometry> getFrameGeometry(RenderwareBinaryStream.Frame frame){
        int frameIndex = BinaryStreamHelpers.getStructFrameList(frameList).frames().indexOf(frame);
        for(int i = 0; i < atomicList.size(); i++) {
            RenderwareBinaryStream.StructAtomic structAtomic = BinaryStreamHelpers.getStructAtomic(atomicList.get(i));
            if(structAtomic.frameIndex() == frameIndex){
                return Optional.ofNullable(geometryList.get((int)structAtomic.geometryIndex()));
            }
        }
        return Optional.empty();
    }

    public String getFrameName(RenderwareBinaryStream.Frame frame){
        if(BinaryStreamHelpers.getStructFrameList(frameList).frames().contains(frame)){
            int index = BinaryStreamHelpers.getStructFrameList(frameList).frames().indexOf(frame);
            return frameNames.get(index);
        }
        return "";
    }

    public List<RenderwareBinaryStream.Frame> getDummyFrames(){
        List<RenderwareBinaryStream.Frame> frames = new ArrayList<>();
        for(int i = 0; i < frameNames.size(); i++){
            if(frameNames.get(i).endsWith("dummy")){
                frames.add(BinaryStreamHelpers.getStructFrameList(frameList).frames().get(i));
            }
        }
        return frames;
    }

    public float wheelSize(){
        Optional<RenderwareBinaryStream.Frame> frame = getFrameByName("wheel");
        if(!frame.isPresent()) return - 1;
        Optional<Geometry> geometry = getFrameGeometry(frame.get());
        if(!geometry.isPresent()) return - 1;
        return ((geometry.get().maxZ - geometry.get().minZ) / 2f) * 0.35f;
    }

    public void renderWheel(float brightness, PlayerEntity player){
        Optional<RenderwareBinaryStream.Frame> frame = getFrameByName("wheel");
        if(!frame.isPresent()) return;
        Optional<Geometry> geometry = getFrameGeometry(frame.get());
        if(!geometry.isPresent()) return;
        geometry.get().render(brightness, (float)player.x, (float)player.y + player.standingEyeHeight, (float)player.z);
    }

    public void render(double x, double y, double z, float brightness, PlayerEntity player){
        GL11.glPushMatrix();
        GL11.glRotatef(180.0f, 0.0f, 1.0f, 0.0f);
        for(int i = 0; i < atomicList.size(); i++){
            RenderwareBinaryStream.StructAtomic structAtomic = BinaryStreamHelpers.getStructAtomic(atomicList.get(i));
            if(modelBlacklist.contains(frameNames.get((int)structAtomic.frameIndex()))){
                continue;
            }
            RenderwareBinaryStream.Frame frame = BinaryStreamHelpers.getStructFrameList(frameList).frames().get((int)structAtomic.frameIndex());
            GL11.glPushMatrix();
            applyFrameTransformations(frame);
            this.geometryList.get((int)structAtomic.geometryIndex()).render(brightness, (float)player.x, (float)player.y + player.standingEyeHeight, (float)player.z);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }

    public Optional<RenderwareBinaryStream.Frame> getFrameByName(String frameName){
        if(!frameNames.contains(frameName)) return Optional.empty();
        return Optional.ofNullable(BinaryStreamHelpers.getStructFrameList(frameList).frames().get(frameNames.indexOf(frameName)));
    }
    public Vector3f getFrameOffset(RenderwareBinaryStream.Frame frame){
        Vector3f vector3f = new Vector3f(0f, 0f, 0f);
        if(frame.curFrameIdx() != 0){
            Vector3f vector3f1 = getFrameOffset(BinaryStreamHelpers.getStructFrameList(frameList).frames().get(frame.curFrameIdx()));
            vector3f.x += vector3f1.x;
            vector3f.y += vector3f1.y;
            vector3f.z += vector3f1.z;
        }

        vector3f.x += frame.position().x();
        vector3f.y += frame.position().z();
        vector3f.z += -frame.position().y();

        return vector3f;
    }

    private void applyFrameTransformations(RenderwareBinaryStream.Frame frame){
        if(frame.curFrameIdx() != 0){
            applyFrameTransformations(BinaryStreamHelpers.getStructFrameList(frameList).frames().get(frame.curFrameIdx()));
        }
        GL11.glTranslatef(frame.position().x(), frame.position().z(), -frame.position().y());
    }
}
