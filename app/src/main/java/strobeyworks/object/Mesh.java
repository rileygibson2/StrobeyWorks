package strobeyworks.object;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.util.HashMap;
import java.util.Map;

import strobeyworks.SWMain;
import strobeyworks.logger.Logger;
import strobeyworks.platform.ShaderManager;
import strobeyworks.utils.Vec3;

public class Mesh {
    public enum MeshType {
        FLAT_SHADED,
        SMOOTH_SHADED
    }
    
    private float[][] vertices;
    private int[][] faces;
    private float[][] normals;
    private int[][] normalFaces; // Only used on some models
    
    private MeshType type;
    
    private int stride = 6;
    private int vAO;
    private int vBO;
    
    private Vec3 minBounds;
    private Vec3 maxBounds;
    
    public Mesh(float[][] vertices, int[][] faces, MeshType type) {
        this.vertices = vertices;
        this.faces = faces;
        this.normals = new float[vertices.length][];
        this.type = type;
        calculateBoundingBox();

        if (type==MeshType.FLAT_SHADED) buildVertexFaceNormals();
        else if (type==MeshType.SMOOTH_SHADED) buildSmoothVertexNormals();
        
        check();
    }
    
    public Mesh(float[][] vertices, int[][] faces, float[][] normals, int[][] normalFaces) {
        this.vertices = vertices;
        this.faces = faces;
        this.normals = normals;
        this.normalFaces = normalFaces;
        this.type = MeshType.FLAT_SHADED;
        calculateBoundingBox();
        
        check();
    }
    
    public void setVAO(int vAO) {this.vAO = vAO;}
    public void setVBO(int vBO) {this.vBO = vBO;}
    
    public float[][] getVertices() {return vertices;}
    public int[][] getFaces() {return faces;}
    public int getVAO() {return vAO;}
    public int getVBO() {return vBO;}
    public int getStride() {return stride;}
    public int getVertexCount() {return faces.length*3;}
    
    private void check() {
        // Duplicates
        int duplicates = 0;
        java.util.Set<String> uniquePositions = new java.util.HashSet<>();
        
        for (float[] v : vertices) {
            String key = positionKey(v);
            if (uniquePositions.contains(key)) duplicates++;
            else uniquePositions.add(key);
        }
        Logger.debug("Vertices: "+vertices.length+" Faces: "+faces.length+" Duplicate verts: "+duplicates);
        
        //Bounding box
        Logger.debug("Min Bounding: "+minBounds.x+", "+minBounds.y+", "+minBounds.z);
        Logger.debug("Max Bounding: "+maxBounds.x+", "+maxBounds.y+", "+maxBounds.z);
    }
    
    private void calculateBoundingBox() {
        minBounds = new Vec3(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        maxBounds = new Vec3(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        
        for (float[] v : vertices) {
            if (v[0]<minBounds.x) minBounds.x = v[0];
            if (v[0]>maxBounds.x) maxBounds.x = v[0];
            if (v[1]<minBounds.y) minBounds.y = v[1];
            if (v[1]>maxBounds.y) maxBounds.y = v[1];
            if (v[2]<minBounds.z) minBounds.z = v[2];
            if (v[2]>maxBounds.z) maxBounds.z = v[2];
        }
    }
    
    private String positionKey(float[] v) {
        return Math.round(v[0] * 10000f)+","+Math.round(v[1] * 10000f)+","+Math.round(v[2] * 10000f);
    }
    
    public void buildSmoothVertexNormals() {
        normals = new float[vertices.length][3];
        Map<String, float[]> normalSums = new HashMap<>();
        
        for (int[] face : faces) {
            int i0 = face[0];
            int i1 = face[1];
            int i2 = face[2];
            float[] fNorm = getFaceNormal(vertices[i0], vertices[i1], vertices[i2]);
            
            addNormal(normalSums, vertices[i0], fNorm);
            addNormal(normalSums, vertices[i1], fNorm);
            addNormal(normalSums, vertices[i2], fNorm);
        }
        
        for (int i=0; i<vertices.length; i++) {
            float[] normal = normalSums.get(positionKey(vertices[i]));
            if (normal==null) continue;
            
            float nx = normal[0];
            float ny = normal[1];
            float nz = normal[2];
            
            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len != 0.0f) {
                normals[i][0] = nx / len;
                normals[i][1] = ny / len;
                normals[i][2] = nz / len;
            }
        }
    }
    
    private void addNormal(Map<String, float[]> normalSums, float[] vertex, float[] normal) {
        float[] sum = normalSums.computeIfAbsent(positionKey(vertex), k -> new float[3]);
        sum[0] += normal[0];
        sum[1] += normal[1];
        sum[2] += normal[2];
    }
    
    
    public void buildVertexFaceNormals() {
        this.normals = new float[vertices.length][3];
        
        for (int[] face : faces) {
            int i0 = face[0];
            int i1 = face[1];
            int i2 = face[2];
            
            float[] faceNormal = getFaceNormal(vertices[i0], vertices[i1], vertices[i2]);
            normals[face[0]] = new float[] {faceNormal[0], faceNormal[1], faceNormal[2]};
            normals[face[1]] = new float[] {faceNormal[0], faceNormal[1], faceNormal[2]};
            normals[face[2]] = new float[] {faceNormal[0], faceNormal[1], faceNormal[2]};
        }
    }
    
    private float[] getFaceNormal(float[] v0, float[] v1, float[] v2) {
        
        float e1x = v1[0] - v0[0];
        float e1y = v1[1] - v0[1];
        float e1z = v1[2] - v0[2];
        
        float e2x = v2[0] - v0[0];
        float e2y = v2[1] - v0[1];
        float e2z = v2[2] - v0[2];
        
        float nx = e1y * e2z - e1z * e2y;
        float ny = e1z * e2x - e1x * e2z;
        float nz = e1x * e2y - e1y * e2x;
        
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len != 0.0f) {
            nx /= len;
            ny /= len;
            nz /= len;
        }
        return new float[] {nx, ny, nz};
    }
    
    public float[] getVertexBuffer() {
        float[] buffer = new float[faces.length*3*3];
        int i = 0;
        for (int[] face : faces) {
            for (int f : face) {
                buffer[i] = vertices[f][0];
                buffer[i+1] = vertices[f][1];
                buffer[i+2] = vertices[f][2];
                i+=3;
            }
        }
        return buffer;
    }
    
    public void initBuffers() {
        // Combine vertices and normals
        float[] combined = new float[faces.length*3*stride];
        
        int i = 0;
        for (int faceI = 0; faceI < faces.length; faceI++) {
            int[] face = faces[faceI];
            
            for (int corner=0; corner<3; corner++) {
                int vertI = face[corner];
                
                int normI = vertI;
                if (normalFaces!=null) normI = normalFaces[faceI][corner];
                
                combined[i] = vertices[vertI][0];
                combined[i+1] = vertices[vertI][1];
                combined[i+2] = vertices[vertI][2];
                combined[i+3] = normals[normI][0];
                combined[i+4] = normals[normI][1];
                combined[i+5] = normals[normI][2];
                i += 6;
            }
        }
        
        ShaderManager sM = SWMain.getShaderManager();
        setVAO(glGenVertexArrays());
        setVBO(glGenBuffers());
        sM.bindVAO(vAO);
        sM.bindVBO(vBO);
        
        glBufferData(GL_ARRAY_BUFFER, combined, GL_STATIC_DRAW);
        
        int vStride = stride * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, vStride, 0);
        glEnableVertexAttribArray(0);
        
        glVertexAttribPointer(1, 3, GL_FLOAT, false, vStride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        
        sM.bindVAO(0);
        sM.bindVBO(0);
    }
}
