package ralf2oo2.carmod.vehicle;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.opengl.GL11;
import ralf2oo2.carmod.Utils.BinaryStreamHelpers;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;
import ralf2oo2.carmod.client.render.Geometry;
import ralf2oo2.carmod.registry.VehicleTextureRegistry;

import java.util.ArrayList;
import java.util.List;

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

    private void applyFrameTransformations(RenderwareBinaryStream.Frame frame){
        if(frame.curFrameIdx() != 0){
            applyFrameTransformations(BinaryStreamHelpers.getStructFrameList(frameList).frames().get(frame.curFrameIdx()));
        }
        GL11.glTranslatef(frame.position().x(), frame.position().z(), -frame.position().y());
    }
}
