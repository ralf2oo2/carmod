package ralf2oo2.carmod.client.render;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.opengl.GL11;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CarModel {
    private String name;
    private static List<CarModel> carModels = new ArrayList<>();
    private RenderwareBinaryStream geometryData;
    private RenderwareBinaryStream textureData;
    private List<Geometry> geometryList = new ArrayList<>();
    private List<RenderwareBinaryStream> atomicList;
    private List<RenderwareBinaryStream> textureList;
    private RenderwareBinaryStream.ListWithHeader frameList;
    private List<String> frameNames = new ArrayList<>();

    public static CarModel getCarModel(String name){
        if(name == null || name.isEmpty()){
            return null;
        }
        Optional<CarModel> optCarModel = carModels.stream().findAny().filter((carModel) -> carModel.name.equals(name));
        if(optCarModel.isPresent()){
            return optCarModel.get();
        }
        else {
            CarModel carModel = new CarModel(name);
            carModels.add(carModel);
            return carModel;
        }
    }
    public CarModel(String name){
        this.name = name;
        String path = FabricLoader.getInstance().getConfigDir() + "/";
        try{
            System.out.println(path + name + ".dff");
            this.geometryData = RenderwareBinaryStream.fromFile(path + name + ".dff");
            this.textureData = RenderwareBinaryStream.fromFile(path + name + ".txd");
        } catch (Exception e){
            System.out.println("Failed to load car model");
        }
        loadData();
    }

    public String getName(){
        return name;
    }

    public void loadData(){
        this.textureList = new ArrayList<>();
        ((RenderwareBinaryStream.ListWithHeader) textureData.body()).entries().forEach((entry) -> {
            if(entry.code().name() == "TEXTURE_NATIVE"){
                textureList.add(entry);
            }
        });
        if(TxdTextureRegistry.textureCount() == 0){
            loadTextures();
        }
        RenderwareBinaryStream.ListWithHeader geometryList = (RenderwareBinaryStream.ListWithHeader)((RenderwareBinaryStream.ListWithHeader) geometryData.body()).entries().get(1).body();

        this.geometryList.clear();
        for(RenderwareBinaryStream geometryBinaryStream : geometryList.entries()){
            this.geometryList.add(new Geometry(geometryBinaryStream));
        }

        this.frameList = (RenderwareBinaryStream.ListWithHeader)((RenderwareBinaryStream.ListWithHeader) geometryData.body()).entries().get(0).body();
        this.atomicList = ((RenderwareBinaryStream.ListWithHeader) geometryData.body()).entries().stream().filter((entry) -> entry.code().name().equals("ATOMIC")).toList();

        RenderwareBinaryStream.ListWithHeader entries = (RenderwareBinaryStream.ListWithHeader)((RenderwareBinaryStream.ListWithHeader)((RenderwareBinaryStream.ListWithHeader) geometryData.body()).entries().get(0).body());
        for(RenderwareBinaryStream stream : entries.entries()){
            byte[] array = (byte[])stream.body();
            byte[] name = Arrays.copyOfRange(array, 12, array.length);
            String namestr = new String(name, StandardCharsets.UTF_8);
            System.out.println(namestr);
            frameNames.add(namestr);
        }
    }

    public void render(double x, double y, double z){
        GL11.glTranslatef((float)x, (float)y, (float)z);
        for(int i = 0; i < atomicList.size(); i++){
            RenderwareBinaryStream.StructAtomic structAtomic = getStructAtomic(atomicList.get(i));
            RenderwareBinaryStream.Frame frame = getStructFrameList(frameList).frames().get((int)structAtomic.frameIndex());
            GL11.glPushMatrix();
            applyFrameTransformations(frame);
            this.geometryList.get((int)structAtomic.geometryIndex()).render();
            GL11.glPopMatrix();
        }
    }

    private void loadTextures(){
        for(int i = 0; i < textureList.size(); i++){
            RenderwareBinaryStream.StructTextureData textureData = getStructTextureData(textureList.get(i));
            TxdTextureRegistry.registerTexture(textureData);
        }
    }

    private void applyFrameTransformations(RenderwareBinaryStream.Frame frame){
        if(frame.curFrameIdx() != 0){
            applyFrameTransformations(getStructFrameList(frameList).frames().get(frame.curFrameIdx()));
        }
        GL11.glTranslatef(frame.position().x(), frame.position().z(), -frame.position().y());
    }

    private RenderwareBinaryStream.StructAtomic getStructAtomic(RenderwareBinaryStream stream){
        return (RenderwareBinaryStream.StructAtomic) ((RenderwareBinaryStream.ListWithHeader)stream.body()).header();
    }

    private RenderwareBinaryStream.StructFrameList getStructFrameList(RenderwareBinaryStream.ListWithHeader stream){
        return (RenderwareBinaryStream.StructFrameList) stream.header();
    }

    private RenderwareBinaryStream.StructTextureData getStructTextureData(RenderwareBinaryStream stream){
        return (RenderwareBinaryStream.StructTextureData) ((RenderwareBinaryStream.ListWithHeader)stream.body()).header();
    }
}
