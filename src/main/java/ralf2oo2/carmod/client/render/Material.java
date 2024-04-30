package ralf2oo2.carmod.client.render;

import org.lwjgl.util.vector.Vector4f;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;

public class Material {
    int flags;
    Rgba color;
    boolean hasTexture;
    float ambient;
    float specular;
    float diffuse;
    Texture texture;

    public Material(int flags, Rgba color, float ambient, float specular, float diffuse) {
        this.flags = flags;
        this.color = color;
        this.hasTexture = false;
        this.ambient = ambient;
        this.specular = specular;
        this.diffuse = diffuse;
    }

    public Material(int flags, Rgba color, float ambient, float specular, float diffuse, int textureFilterFlags, String textureName, String textureMaskName) {
        this.flags = flags;
        this.color = color;
        this.hasTexture = true;
        this.ambient = ambient;
        this.specular = specular;
        this.diffuse = diffuse;
        this.texture = new Texture(textureFilterFlags, textureName, textureMaskName);
    }
}

class Texture{
    public int filterFlags;
    public String name;
    public String maskName;

    public Texture(int filterFlags, String name, String maskName) {
        this.filterFlags = filterFlags;
        this.name = name;
        this.maskName = maskName;
    }
}
