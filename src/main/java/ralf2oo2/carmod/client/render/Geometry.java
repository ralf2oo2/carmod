package ralf2oo2.carmod.client.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import ralf2oo2.carmod.Utils.RenderwareBinaryStream;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private int vao;
    private int[] ibos;
    private int[] vertexCounts;
    private String name;
    private RenderwareBinaryStream geometryBinaryStream;

    //Geometry data
    private List<Material> materials;
    private Map<Integer, List<RenderwareBinaryStream.Triangle>> trianglesByMaterial;
    List<Vertex>[] verticesByMaterial;
    List<Integer>[] indexByMaterial;

    public Geometry(RenderwareBinaryStream geometryBinaryStream){
        this.geometryBinaryStream = geometryBinaryStream;
        if(shaderProgram == 0){
            compileShaders();
        }
        populateTriangles();
        initBuffers();
    }

    public String getName(){
        return name;
    }

    public void populateTriangles(){
        materials = getMaterialListForGeometry();
        RenderwareBinaryStream.StructGeometry geometry = getStructGeometry(geometryBinaryStream);
        List<RenderwareBinaryStream.Triangle> triangles =  geometry.geometry().triangles();
        trianglesByMaterial = new HashMap<>();
        for (RenderwareBinaryStream.Triangle triangle : triangles) {
            trianglesByMaterial
                    .computeIfAbsent(triangle.materialId(), k -> new ArrayList<>())
                    .add(triangle);
        }
    }

//    public void populateVertexList(){
//        materials = getMaterialListForGeometry();
//
//
//        verticesByMaterial = new List[materials.size()];
//        indexByMaterial = new List[materials.size()]; // TODO: actual index format
//
//        for(int i = 0; i < verticesByMaterial.length; i++){
//            verticesByMaterial[i] = new ArrayList<>();
//            indexByMaterial[i] = new ArrayList<>();
//        }
//
//        for(RenderwareBinaryStream.Triangle triangle : triangles){
//
//            int materialId = triangle.materialId();
//
//            int[] triangleVertexIndices = {
//              triangle.vertex1(),
//              triangle.vertex2(),
//              triangle.vertex3()
//            };
//
//            for(int i = 0; i < verticesByMaterial.length; i++){
//                int vertIndex = triangleVertexIndices[i];
//
//                Vertex vertex = new Vertex();
//
//                // Fill vertex position
//                RenderwareBinaryStream.Vector3d vert = vertices.get(vertIndex);
//                vertex.x = vert.x();
//                vertex.y = vert.y();
//                vertex.z = vert.z();
//
//                // Fill vertex normal
//                RenderwareBinaryStream.Vector3d normal = normals.get(vertIndex);
//                vertex.normalX = normal.x();
//                vertex.normalY = normal.y();
//                vertex.normalZ = normal.z();
//
//                // Fill vertex UV coordinates
//                RenderwareBinaryStream.TexCoord uv = uvLayer.texCoords().get(vertIndex);
//                vertex.uOffset = uv.u();
//                vertex.vOffset = uv.v();
//
//                // Fill vertex color from the material (using materialId)
//                vertex.r = materials.get(materialId).color.r;
//                vertex.g = materials.get(materialId).color.g;
//                vertex.b = materials.get(materialId).color.b;
//                vertex.a = materials.get(materialId).color.a;
//
//                // Add this vertex to the verticesByMaterial list for the given material
//                // Check if this vertex already exists in verticesByMaterial to avoid duplication
//                List<Vertex> currentVertices = verticesByMaterial[materialId];
//                if (!currentVertices.contains(vertex)) {
//                    currentVertices.add(vertex);
//                } else {
//                    System.out.println("duplicate vertex");
//                }
//
//                // Find the index of this vertex (whether newly added or already present)
//                int vertexIndexInMaterial = currentVertices.indexOf(vertex);
//
//                // Add this index to the index buffer for this material
//                indexByMaterial[materialId].add(vertexIndexInMaterial);
//            }
//        }
//    }

    public void initBuffers(){

        GL20.glUseProgram(shaderProgram);
        vbo = GL15.glGenBuffers();
        vao = GL30.glGenVertexArrays();

        GL30.glBindVertexArray(vao);

        RenderwareBinaryStream.StructGeometry geometry = getStructGeometry(geometryBinaryStream);

        ArrayList<RenderwareBinaryStream.Vector3d> vertices = geometry.morphTargets().get(0).vertices();
        ArrayList<RenderwareBinaryStream.Vector3d> normals = geometry.morphTargets().get(0).normals();
        ArrayList<RenderwareBinaryStream.Rgba> colors = geometry.geometry().prelitColors();
        RenderwareBinaryStream.UvLayer uvLayer = geometry.geometry().uvLayers().get(0);

        int totalVertices = vertices.size();

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(totalVertices * 12); // TODO: Replace 12 with vertex size later

        for (int i = 0; i < totalVertices; i++) {
            vertexBuffer.put(vertices.get(i).x()).put(vertices.get(i).y()).put(vertices.get(i).z());
            vertexBuffer.put(normals.get(i).x()).put(normals.get(i).y()).put(normals.get(i).z());
            vertexBuffer.put(uvLayer.texCoords().get(i).v()).put(uvLayer.texCoords().get(i).u());
            if(geometry.isPrelit()){
                vertexBuffer.put(colors.get(i).r() / 255f).put(colors.get(i).g() / 255f).put(colors.get(i).b() / 255f).put(colors.get(i).a() / 255f);
            }
            else{
                vertexBuffer.put(1f).put(1f).put(1f).put(1f);
            }

        }
        vertexBuffer.flip();

        // Bind and upload to vbo
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);

        int stride = 12 * Float.BYTES;

        int positionLocation = GL20.glGetAttribLocation(shaderProgram, "vertexPosition");
        int normalLocation = GL20.glGetAttribLocation(shaderProgram, "normalPosition");
        int uvLocation = GL20.glGetAttribLocation(shaderProgram, "vertexUV");
        int colorLocation = GL20.glGetAttribLocation(shaderProgram, "matColor");

        // Position
        GL20.glVertexAttribPointer(positionLocation, 3, GL11.GL_FLOAT, false, stride, 0);
        GL20.glEnableVertexAttribArray(positionLocation);

        // Normal
        GL20.glVertexAttribPointer(normalLocation, 3, GL11.GL_FLOAT, false, stride, 3 * Float.BYTES);
        GL20.glEnableVertexAttribArray(normalLocation);

        // UV
        GL20.glVertexAttribPointer(uvLocation, 2, GL11.GL_FLOAT, false, stride, 6 * Float.BYTES);
        GL20.glEnableVertexAttribArray(uvLocation);

        // Color
        GL20.glVertexAttribPointer(colorLocation, 4, GL11.GL_FLOAT, false, stride, 8 * Float.BYTES);
        GL20.glEnableVertexAttribArray(colorLocation);

        ibos = new int[trianglesByMaterial.size()];

        for (int materialId = 0; materialId < trianglesByMaterial.size(); materialId++) {
            ibos[materialId] = GL15.glGenBuffers();

            ArrayList<RenderwareBinaryStream.Triangle> triangles = (ArrayList<RenderwareBinaryStream.Triangle>) trianglesByMaterial.get(materialId);

            IntBuffer indexBuffer = BufferUtils.createIntBuffer(triangles.size() * 3);
            for (int triangleIndex = 0; triangleIndex < triangles.size(); triangleIndex++) {
                indexBuffer.put(triangles.get(triangleIndex).vertex1()).put(triangles.get(triangleIndex).vertex2()).put(triangles.get(triangleIndex).vertex3());
            }
            indexBuffer.flip(); // Flip buffer for writing

            // Bind and upload the index buffer to the IBO
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibos[materialId]);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);
        }

        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL20.glUseProgram(0);
    }

    private int calculateTotalVertices(){
        int totalVertices = 0;
        for (int i = 0; i < verticesByMaterial.length; i++) {
            totalVertices += verticesByMaterial[i].size();
        }
        return  totalVertices;
    }

    public void render(float brightness, float playerX, float playerY, float playerZ){
        GL11.glPushMatrix();
        GL11.glRotatef(-90, 1f, 0f, 0f);

        GL30.glBindVertexArray(vao);

        GL20.glUseProgram(shaderProgram);

        int textureSamplerLocation = GL20.glGetUniformLocation(shaderProgram, "textureSampler");
        int useTextureLocation = GL20.glGetUniformLocation(shaderProgram, "useTexture");
        int brightnessLocation = GL20.glGetUniformLocation(shaderProgram, "brightness");
        int ambientLocation = GL20.glGetUniformLocation(shaderProgram, "ambientIntensity");
        int diffuseLocation = GL20.glGetUniformLocation(shaderProgram, "diffuseIntensity");
        int specularLocation = GL20.glGetUniformLocation(shaderProgram, "specularIntensity");
        int lightPosLocation = GL20.glGetUniformLocation(shaderProgram, "lightPos");
        int lightColorLocation = GL20.glGetUniformLocation(shaderProgram, "lightColor");
        int viePosLocation = GL20.glGetUniformLocation(shaderProgram, "viewPos");

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

        for (int materialId = 0; materialId < trianglesByMaterial.size(); materialId++) {
            // Bind the IBO for this material
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibos[materialId]);

            if(materials.get(materialId).hasTexture){
                int textureId = TxdTextureRegistry.getTextureId(materials.get(materialId).texture.name.replace("\0", ""));
                //((Minecraft)FabricLoader.getInstance().getGameInstance()).textureManager.getTextureId("/gui/furnace.png");
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
                GL20.glUniform1i(textureSamplerLocation, 0);
                GL20.glUniform1i(useTextureLocation, 1);
            } else {
                GL20.glUniform1i(useTextureLocation, 0);
            }

            GL20.glUniformMatrix4(modelViewMatrixLocation, false, matrixBuffer);
            GL20.glUniformMatrix4(projectionMatrixLocation, false, projectionBuffer);

            GL20.glUniform1f(brightnessLocation, brightness);//
            GL20.glUniform1f(ambientLocation, materials.get(materialId).ambient);//
            GL20.glUniform1f(diffuseLocation, materials.get(materialId).diffuse);
            GL20.glUniform1f(specularLocation, materials.get(materialId).specular);

            GL20.glUniform3f(lightPosLocation, 0.0f, 00.0f, 10.0f);
            GL20.glUniform3f(lightColorLocation, 1, 1, 1);
            GL20.glUniform3f(viePosLocation, playerX, playerY, playerZ);

            // Draw the triangles for this material using the indices from the IBO
            GL11.glDrawElements(GL11.GL_TRIANGLES, trianglesByMaterial.get(materialId).size(), GL11.GL_UNSIGNED_INT, 0);
        }
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL20.glUseProgram(0);
        GL11.glPopMatrix();
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

    private static final float EPSILON = 1e-6f;
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Vertex vertex = (Vertex) obj;

        return Math.abs(vertex.x - x) < EPSILON &&
                Math.abs(vertex.y - y) < EPSILON &&
                Math.abs(vertex.z - z) < EPSILON &&
                // Include checks for normal, UV, and color with similar logic...
                Math.abs(vertex.uOffset - uOffset) < EPSILON &&
                Math.abs(vertex.vOffset - vOffset) < EPSILON &&
                Float.compare(vertex.r, r) == 0 &&
                Float.compare(vertex.g, g) == 0 &&
                Float.compare(vertex.b, b) == 0 &&
                Float.compare(vertex.a, a) == 0;
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (z != +0.0f ? Float.floatToIntBits(z) : 0);
        result = 31 * result + (normalX != +0.0f ? Float.floatToIntBits(normalX) : 0);
        result = 31 * result + (normalY != +0.0f ? Float.floatToIntBits(normalY) : 0);
        result = 31 * result + (normalZ != +0.0f ? Float.floatToIntBits(normalZ) : 0);
        result = 31 * result + (uOffset != +0.0f ? Float.floatToIntBits(uOffset) : 0);
        result = 31 * result + (vOffset != +0.0f ? Float.floatToIntBits(vOffset) : 0);
        result = 31 * result + (r != +0.0f ? Float.floatToIntBits(r) : 0);
        result = 31 * result + (g != +0.0f ? Float.floatToIntBits(g) : 0);
        result = 31 * result + (b != +0.0f ? Float.floatToIntBits(b) : 0);
        result = 31 * result + (a != +0.0f ? Float.floatToIntBits(a) : 0);
        return result;
    }
}
