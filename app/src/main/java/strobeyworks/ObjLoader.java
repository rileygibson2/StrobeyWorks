package strobeyworks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import strobeyworks.object.Mesh;
import strobeyworks.object.Mesh.MeshType;

public class ObjLoader {
    
    private static List<float[]> vertices;
    private static List<float[]> normals;
    private static List<int[]> faces;
    private static List<int[]> normalFaces;

    private ObjLoader() {}
    
    public static Mesh loadMesh(String resourcePath, boolean useResourceNormals, MeshType type) {
        vertices = new ArrayList<>();
        normals = new ArrayList<>();
        faces = new ArrayList<>();
        normalFaces = new ArrayList<>();
        
        InputStream in = ObjLoader.class.getResourceAsStream("/meshes/"+resourcePath);
        if (in==null) {
            throw new RuntimeException("OBJ resource not found: /meshes/" + resourcePath);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()||line.startsWith("#")) continue;
                
                if (line.startsWith("v ")) parseVertexLine(line);
                else if (line.startsWith("f ")) parseFaceLine(line);
                else if (line.startsWith("vn ")) parseVertexNormalLine(line);
                //else if (line.startsWith("vt ")) parseTexCoord(line);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load OBJ: " + resourcePath, e);
        }
        
        float[][] vertOut = new float[vertices.size()][];
        for (int i=0; i<vertices.size(); i++) vertOut[i] = vertices.get(i);
        
        float[][] normOut = new float[normals.size()][];
        for (int i=0; i<normals.size(); i++) normOut[i] = normals.get(i);
        
        int[][] faceOut = new int[faces.size()][];
        for (int i=0; i<faces.size(); i++) faceOut[i] = faces.get(i);

        int[][] normalFaceOut = new int[normalFaces.size()][];
        for (int i=0; i<normalFaces.size(); i++) normalFaceOut[i] = normalFaces.get(i);
        
        if (useResourceNormals) return new Mesh(vertOut, faceOut, normOut, normalFaceOut);
        else return new Mesh(vertOut, faceOut, type);
    }
    
    private static void parseVertexLine(String token) {
        String[] parts = token.split("\\s+");
        
        float x = Float.parseFloat(parts[1]);
        float y = Float.parseFloat(parts[2]);
        float z = Float.parseFloat(parts[3]);
        vertices.add(new float[]{x, y, z});
    }
    
    private static void parseVertexNormalLine(String token) {
        String[] parts = token.split("\\s+");
        
        float x = Float.parseFloat(parts[1]);
        float y = Float.parseFloat(parts[2]);
        float z = Float.parseFloat(parts[3]);
        normals.add(new float[]{x, y, z});
    }
    
    private static void parseFaceLine(String token) {
        String[] parts = token.trim().split("\\s+");
        
        int vertexCount = parts.length-1;
        int[] faceVerts = new int[vertexCount];
        int[] faceNorms = new int[vertexCount];
        
        for (int i = 0; i<vertexCount; i++) {
            String faceToken = parts[i + 1];
            faceVerts[i] = parseObjVertexIndex(faceToken);
            faceNorms[i] = parseObjNormalIndex(faceToken);
        }
        
        for (int i=1; i<vertexCount-1; i++) {
            faces.add(new int[] {
                faceVerts[0],
                faceVerts[i],
                faceVerts[i + 1]
            });
            
            normalFaces.add(new int[] {
                faceNorms[0],
                faceNorms[i],
                faceNorms[i + 1]
            });
        }
    }
    
    private static int parseObjVertexIndex(String token) {
        String[] parts = token.split("/", -1);
        return Integer.parseInt(parts[0]) - 1;
    }
    
    private static int parseObjNormalIndex(String token) {
        String[] parts = token.split("/", -1);
        if (parts.length < 3 || parts[2].isEmpty()) return -1;
        return Integer.parseInt(parts[2]) - 1;
    }
}
