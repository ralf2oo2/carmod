package ralf2oo2.carmod.client.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class Geometry {
    private static final String vertexShaderSource =
            "#version 330 core\n"
                    + "in vec3 vertexPosition;\n"
                    + "in vec3 normalPosition;\n"
                    + "in vec2 vertexUV;\n"
                    + "in vec4 matColor;\n"
                    + "uniform mat4 modelViewMatrix;\n"
                    + "uniform mat4 projectionMatrix;\n"
                    + "out vec3 outNormal;\n"
                    + "out vec2 outUV;\n"
                    + "out vec4 outColor;\n"
                    + "out vec3 outFragPos;\n"
                    + "void main() {\n"
                    + "    outNormal = normalPosition;\n"
                    + "    outUV = vertexUV;\n"
                    + "    outColor = matColor;\n"
                    + "    gl_Position = projectionMatrix * modelViewMatrix * vec4(vertexPosition, 1.0);\n"
                    + "    mat4 cameraMatrix = inverse(modelViewMatrix);\n"
                    + "    mat4 modelMatrix = modelViewMatrix * cameraMatrix;\n"
                    + "    outFragPos = vec3(modelMatrix * vec4(vertexPosition, 1.0));\n"
                    + "}";
    private static final String fragmentShaderSource =
            "#version 330 core\n"
                    + "in vec3 outNormal;\n"
                    + "in vec2 outUV;\n"
                    + "in vec4 outColor;\n"
                    + "in vec3 outFragPos;\n"
                    + "out vec4 fragColor;\n"
                    + "uniform sampler2D textureSampler;\n"
                    + "uniform int useTexture;\n"
                    + "uniform float brightness;\n"
                    + "uniform float ambientIntensity;\n"
                    + "uniform float diffuseIntensity;\n"
                    + "uniform float specularIntensity;\n"
                    + "uniform vec3 lightPos;\n"
                    + "uniform vec3 lightColor;\n"
                    + "uniform vec3 viewPos;\n"
                    + "void main() {\n"
                    + "    vec3 lightDir = normalize(lightPos - outFragPos);\n"
                    + "    vec3 norm = normalize(outNormal);\n"
                    + "    float diff = max(dot(norm, lightDir), 0.0);\n"
                    + "    vec3 diffVec = diff * lightColor;\n"
                    + "    vec3 ambientColor = ambientIntensity * lightColor;\n"
                    + "    vec3 viewDir = normalize(viewPos - outFragPos);\n"
                    + "    vec3 reflectDir = reflect(-lightDir, norm);\n"
                    + "    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);\n"
                    + "    vec3 specular = specularIntensity * spec * lightColor;\n"
                    + "    vec4 texColor;\n"
                    + "    if(useTexture > 0){\n"
                    + "    texColor = texture2D(textureSampler, outUV);\n"
                    + "    } else {\n"
                    + "    texColor = vec4(1.0);\n"
                    + "    }\n"
                    + "    vec4 finalColor = vec4((ambientColor + diffVec + specular) * outColor.rgb, outColor.a) * brightness;\n"
                    + "    fragColor = finalColor * texColor;\n"
                    + "}";

    private static int shaderProgram;
    private int vbo;
    private int[] vaos;
    private int[] vertexCounts;
    private String name;
    private RenderwareBinaryStream geometryBinaryStream;

    //Geometry data
    private List<Material> materials;
    List<Vertex>[] verticesByMaterial;

    public Geometry(RenderwareBinaryStream geometryBinaryStream){
        this.geometryBinaryStream = geometryBinaryStream;
        if(shaderProgram == 0){
            compileShaders();
        }
        populateVertexList();
        initBuffers();
    }

    public String getName(){
        return name;
    }

    public void populateVertexList(){
        materials = getMaterialListForGeometry();
        RenderwareBinaryStream.StructGeometry geometry = getStructGeometry(geometryBinaryStream);

        verticesByMaterial = new List[materials.size()];
        for(int i = 0; i < verticesByMaterial.length; i++){
            verticesByMaterial[i] = new ArrayList<>();
        }

        List<RenderwareBinaryStream.Triangle> triangles =  geometry.geometry().triangles();
        ArrayList<RenderwareBinaryStream.Vector3d> vertices = geometry.morphTargets().get(0).vertices();
        ArrayList<RenderwareBinaryStream.Vector3d> normals = geometry.morphTargets().get(0).normals();
        RenderwareBinaryStream.UvLayer uvLayer = geometry.geometry().uvLayers().get(0);

        for(RenderwareBinaryStream.Triangle triangle : triangles){
            Vertex vertex1 = new Vertex();
            Vertex vertex2 = new Vertex();
            Vertex vertex3 = new Vertex();

            RenderwareBinaryStream.Vector3d vert1 = vertices.get(triangle.vertex1());
            RenderwareBinaryStream.Vector3d vert2 = vertices.get(triangle.vertex2());
            RenderwareBinaryStream.Vector3d vert3 = vertices.get(triangle.vertex3());

            vertex1.x = vert1.x();
            vertex1.y = vert1.y();
            vertex1.z = vert1.z();
            vertex2.x = vert2.x();
            vertex2.y = vert2.y();
            vertex2.z = vert2.z();
            vertex3.x = vert3.x();
            vertex3.y = vert3.y();
            vertex3.z = vert3.z();

            vertex1.r = materials.get(triangle.materialId()).color.r;
            vertex2.r = materials.get(triangle.materialId()).color.r;
            vertex3.r = materials.get(triangle.materialId()).color.r;
            vertex1.g = materials.get(triangle.materialId()).color.g;
            vertex2.g = materials.get(triangle.materialId()).color.g;
            vertex3.g = materials.get(triangle.materialId()).color.g;
            vertex1.b = materials.get(triangle.materialId()).color.b;
            vertex2.b = materials.get(triangle.materialId()).color.b;
            vertex3.b = materials.get(triangle.materialId()).color.b;
            vertex1.a = materials.get(triangle.materialId()).color.a;
            vertex2.a = materials.get(triangle.materialId()).color.a;
            vertex3.a = materials.get(triangle.materialId()).color.a;

            RenderwareBinaryStream.TexCoord uv1 = uvLayer.texCoords().get(triangle.vertex1());
            vertex1.uOffset = uv1.u();
            vertex1.vOffset = uv1.v();
            RenderwareBinaryStream.TexCoord uv2 = uvLayer.texCoords().get(triangle.vertex2());
            vertex2.uOffset = uv2.u();
            vertex2.vOffset = uv2.v();
            RenderwareBinaryStream.TexCoord uv3 = uvLayer.texCoords().get(triangle.vertex3());
            vertex3.uOffset = uv3.u();
            vertex3.vOffset = uv3.v();
            RenderwareBinaryStream.Vector3d normal1 = normals.get(triangle.vertex1());
            vertex1.normalX = normal1.x();
            vertex1.normalY = normal1.y();
            vertex1.normalZ = normal1.z();
            RenderwareBinaryStream.Vector3d normal2 = normals.get(triangle.vertex2());
            vertex2.normalX = normal2.x();
            vertex2.normalY = normal2.y();
            vertex2.normalZ = normal2.z();
            RenderwareBinaryStream.Vector3d normal3 = normals.get(triangle.vertex3());
            vertex3.normalX = normal3.x();
            vertex3.normalY = normal3.y();
            vertex3.normalZ = normal3.z();

            verticesByMaterial[triangle.materialId()].add(vertex1);
            verticesByMaterial[triangle.materialId()].add(vertex2);
            verticesByMaterial[triangle.materialId()].add(vertex3);
        }
    }

    public void initBuffers(){

        int totalVertices = calculateTotalVertices();

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(totalVertices * 12); // TODO: Replace 12 with vertex size later

        for (int i = 0; i < verticesByMaterial.length; i++) {
            for (Vertex vertex : verticesByMaterial[i]) {
                vertexBuffer.put(vertex.x).put(vertex.y).put(vertex.z);
                vertexBuffer.put(vertex.normalX).put(vertex.normalY).put(vertex.normalZ);
                vertexBuffer.put(vertex.uOffset).put(vertex.vOffset);
                vertexBuffer.put(vertex.r / 255f).put(vertex.g / 255f).put(vertex.b / 255f).put(vertex.a / 255f);
            }
        }
        vertexBuffer.flip();

        // Generate VBO
        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

        // Create VAOs for each material
        vaos = new int[verticesByMaterial.length];
        vertexCounts = new int[verticesByMaterial.length];

        int offset = 0;
        for (int i = 0; i < verticesByMaterial.length; i++) {
            if (verticesByMaterial[i].size() == 0) continue; // Skip if there are no vertices
            vaos[i] = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(vaos[i]);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            int stride = 12 * Float.BYTES;

            int positionLocation = GL20.glGetAttribLocation(shaderProgram, "vertexPosition");
            int normalLocation = GL20.glGetAttribLocation(shaderProgram, "normalPosition");
            int uvLocation = GL20.glGetAttribLocation(shaderProgram, "vertexUV");
            int colorLocation = GL20.glGetAttribLocation(shaderProgram, "matColor");

            GL20.glVertexAttribPointer(positionLocation, 3, GL11.GL_FLOAT, false, stride, offset * Float.BYTES);
            GL20.glVertexAttribPointer(normalLocation, 3, GL11.GL_FLOAT, false, stride, (offset + 3) * Float.BYTES);
            GL20.glVertexAttribPointer(uvLocation, 2, GL11.GL_FLOAT, false, stride, (offset + 6) * Float.BYTES);
            GL20.glVertexAttribPointer(colorLocation, 4, GL11.GL_FLOAT, true, stride, (offset + 8) * Float.BYTES);

            GL20.glEnableVertexAttribArray(positionLocation);
            GL20.glEnableVertexAttribArray(normalLocation);
            GL20.glEnableVertexAttribArray(uvLocation);
            GL20.glEnableVertexAttribArray(colorLocation);

            vertexCounts[i] = verticesByMaterial[i].size();
            offset += vertexCounts[i];
        }

        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private int calculateTotalVertices(){
        int totalVertices = 0;
        for (int i = 0; i < verticesByMaterial.length; i++) {
            totalVertices += verticesByMaterial[i].size();
        }
        return  totalVertices;
    }

    public void render(float brightness, float playerX, float playerY, float playerZ){
        GL11.glRotatef(-90, 1f, 0f, 0f);
        for(int i = 0; i < verticesByMaterial.length; i++){
            if(verticesByMaterial[i].size() == 0) continue;
            FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(verticesByMaterial[i].size() * 12);

            for(Vertex vertex : verticesByMaterial[i]){
                vertexBuffer.put(vertex.x).put(vertex.y).put(vertex.z);
                vertexBuffer.put(vertex.normalX).put(vertex.normalY).put(vertex.normalZ);
                vertexBuffer.put(vertex.uOffset).put(vertex.vOffset);
                vertexBuffer.put(vertex.r / 255f).put(vertex.g / 255f).put(vertex.b / 255f).put(vertex.a / 255f);
            }

            vertexBuffer.flip();

            GL20.glUseProgram(shaderProgram);

            int vbo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

            int stride = 12 * Float.BYTES; // 3 position + 2 UV offset


            int textureSamplerLocation = GL20.glGetUniformLocation(shaderProgram, "textureSampler");
            int useTextureLocation = GL20.glGetUniformLocation(shaderProgram, "useTexture");
            int brightnessLocation = GL20.glGetUniformLocation(shaderProgram, "brightness");
            int ambientLocation = GL20.glGetUniformLocation(shaderProgram, "ambientIntensity");
            int diffuseLocation = GL20.glGetUniformLocation(shaderProgram, "diffuseIntensity");
            int specularLocation = GL20.glGetUniformLocation(shaderProgram, "specularIntensity");
            int lightPosLocation = GL20.glGetUniformLocation(shaderProgram, "lightPos");
            int lightColorLocation = GL20.glGetUniformLocation(shaderProgram, "lightColor");
            int viePosLocation = GL20.glGetUniformLocation(shaderProgram, "viewPos");

            int textureUnit = 0;
            if(materials.get(i).hasTexture){
                int textureId = TxdTextureRegistry.getTextureId(materials.get(i).texture.name.replace("\0", ""));
                //((Minecraft)FabricLoader.getInstance().getGameInstance()).textureManager.getTextureId("/gui/furnace.png");
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
                GL20.glUniform1i(textureSamplerLocation, 0);
                GL20.glUniform1i(useTextureLocation, 1);
            } else {
                GL20.glUniform1i(useTextureLocation, 0);
            }

            GL20.glVertexAttribPointer(positionLocation, 3, GL11.GL_FLOAT, false, stride, 0); // Position
            GL20.glVertexAttribPointer(normalLocation, 3, GL11.GL_FLOAT, false, stride, 3 * Float.BYTES); // Position
            GL20.glVertexAttribPointer(uvLocation, 2, GL11.GL_FLOAT, false, stride, 6 * Float.BYTES); // UV offset
            GL20.glVertexAttribPointer(colorLocation, 4, GL11.GL_FLOAT, true, stride, 8 * Float.BYTES); // UV offset

            GL20.glEnableVertexAttribArray(positionLocation); // Position
            GL20.glEnableVertexAttribArray(normalLocation); // Position
            GL20.glEnableVertexAttribArray(uvLocation); // UV offset
            GL20.glEnableVertexAttribArray(colorLocation); // UV offset

            FloatBuffer modelViewMatrixArray = BufferUtils.createFloatBuffer(16);
            FloatBuffer projectionMatrixArray = BufferUtils.createFloatBuffer(16);
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelViewMatrixArray);
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrixArray);

            int modelViewMatrixLocation = GL20.glGetUniformLocation(shaderProgram, "modelViewMatrix");
            int projectionMatrixLocation = GL20.glGetUniformLocation(shaderProgram, "projectionMatrix");
            FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
            FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);
            matrixBuffer.put(modelViewMatrixArray);
            projectionBuffer.put(projectionMatrixArray);
            matrixBuffer.flip();
            projectionBuffer.flip();

            GL20.glUniformMatrix4(modelViewMatrixLocation, false, matrixBuffer);
            GL20.glUniformMatrix4(projectionMatrixLocation, false, projectionBuffer);

            GL20.glUniform1f(brightnessLocation, brightness);//
            GL20.glUniform1f(ambientLocation, materials.get(i).ambient);//
            GL20.glUniform1f(diffuseLocation, materials.get(i).diffuse);
            GL20.glUniform1f(specularLocation, materials.get(i).specular);

            GL20.glUniform3f(lightPosLocation, 0.0f, 00.0f, 10.0f);
            GL20.glUniform3f(lightColorLocation, 1, 1, 1);
            GL20.glUniform3f(viePosLocation, playerX, playerY, playerZ);

            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, verticesByMaterial[i].size());

            GL20.glDisableVertexAttribArray(positionLocation); // Position
            GL20.glDisableVertexAttribArray(normalLocation); // Position
            GL20.glDisableVertexAttribArray(uvLocation); // UV offset
            GL20.glDisableVertexAttribArray(colorLocation); // UV offset

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            GL20.glUseProgram(0);
            GL15.glDeleteBuffers(vbo);
        }
    }

    private RenderwareBinaryStream.StructGeometry getStructGeometry(RenderwareBinaryStream stream){
        return (RenderwareBinaryStream.StructGeometry) ((RenderwareBinaryStream.ListWithHeader)stream.body()).header();
    }

    private List<Material> getMaterialListForGeometry(){
        List<Material> materials = new ArrayList<>();

        RenderwareBinaryStream.ListWithHeader materialList = (RenderwareBinaryStream.ListWithHeader) ((RenderwareBinaryStream.ListWithHeader)geometryBinaryStream.body()).entries().get(0).body();
        RenderwareBinaryStream.StructMaterialList structMaterialList = (RenderwareBinaryStream.StructMaterialList)materialList.header();
        for(int i = 0; i < structMaterialList.materialCount(); i++){
            RenderwareBinaryStream.ListWithHeader material = (RenderwareBinaryStream.ListWithHeader) materialList.entries().get(i).body();
            RenderwareBinaryStream.StructMaterial structMaterial = (RenderwareBinaryStream.StructMaterial)material.header();
            Rgba rgba = new Rgba(structMaterial.color().r(), structMaterial.color().g(), structMaterial.color().b(), structMaterial.color().a());
            if(structMaterial.isTextured() > 0){
                RenderwareBinaryStream.ListWithHeader texture = (RenderwareBinaryStream.ListWithHeader) material.entries().get(0).body();
                RenderwareBinaryStream.StructTexture structTexture = (RenderwareBinaryStream.StructTexture) texture.header();
                RenderwareBinaryStream.StructString textureName = (RenderwareBinaryStream.StructString)texture.entries().get(0).body();
                RenderwareBinaryStream.StructString maskName = (RenderwareBinaryStream.StructString)texture.entries().get(1).body();
                materials.add(new Material((int)structMaterial.flags(), rgba, structMaterial.ambient(), structMaterial.specular(), structMaterial.diffuse(), (int)structTexture.filterFlags(), textureName.value(), maskName.value()));
                continue;
            }
            materials.add(new Material((int)structMaterial.flags(), rgba, structMaterial.ambient(), structMaterial.specular(), structMaterial.diffuse()));
        }
        return  materials;
    }

    private void compileShaders(){
        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

        GL20.glShaderSource(vertexShader, vertexShaderSource);
        GL20.glShaderSource(fragmentShader, fragmentShaderSource);

        GL20.glCompileShader(vertexShader);
        GL20.glCompileShader(fragmentShader);

        boolean failed = false;

        int vertexShaderCompileStatus = GL20.glGetShaderi(vertexShader, GL20.GL_COMPILE_STATUS);
        if(vertexShaderCompileStatus != GL11.GL_TRUE) {
            System.out.println("Vertex Shader Info Log: ");
            System.out.println(GL20.glGetShaderInfoLog(vertexShader, GL20.glGetShaderi(vertexShader, GL20.GL_INFO_LOG_LENGTH)));
            failed = true;
        }

        int fragmentShaderCompileStatus = GL20.glGetShaderi(fragmentShader, GL20.GL_COMPILE_STATUS);
        if(fragmentShaderCompileStatus != GL11.GL_TRUE) {
            System.out.println("Fragment Shader Info Log: ");
            System.out.println(GL20.glGetShaderInfoLog(fragmentShader, GL20.glGetShaderi(fragmentShader, GL20.GL_INFO_LOG_LENGTH)));
            failed = true;
        }

        if(!failed) {
            shaderProgram = GL20.glCreateProgram();

            GL20.glAttachShader(shaderProgram, vertexShader);
            GL20.glAttachShader(shaderProgram, fragmentShader);

            GL20.glLinkProgram(shaderProgram);

            GL20.glDeleteShader(vertexShader);
            GL20.glDeleteShader(fragmentShader);
        }
    }
}

class Vertex {
    static int SIZE = Float.BYTES * 9;
    float x, y, z; // Position
    float normalX, normalY, normalZ;
    float uOffset, vOffset = 0; // UV offsets
    float r, g, b, a = 0;
}
