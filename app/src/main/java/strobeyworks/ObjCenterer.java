package strobeyworks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class ObjCenterer {
    
    private ObjCenterer() {}
    
    public static void center(Path inputPath, Path outputPath) throws IOException {
        Bounds bounds = findBounds(inputPath);
        
        float centerX = (bounds.minX + bounds.maxX) / 2f;
        float centerY = (bounds.minY + bounds.maxY) / 2f;
        float centerZ = (bounds.minZ + bounds.maxZ) / 2f;
        
        try (
            BufferedReader reader = Files.newBufferedReader(inputPath);
            BufferedWriter writer = Files.newBufferedWriter(outputPath)
        ) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("v ")) {
                    writer.write(centerVertexLine(line, centerX, centerY, centerZ));
                } else {
                    writer.write(line);
                }
                
                writer.newLine();
            }
        }
    }
    
    private static Bounds findBounds(Path inputPath) throws IOException {
        Bounds bounds = new Bounds();
        
        try (BufferedReader reader = Files.newBufferedReader(inputPath)) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("v ")) continue;
                
                String[] parts = line.trim().split("\\s+");
                
                float x = Float.parseFloat(parts[1]);
                float y = Float.parseFloat(parts[2]);
                float z = Float.parseFloat(parts[3]);
                
                bounds.include(x, y, z);
            }
        }
        
        if (!bounds.hasVertex) {
            throw new IllegalArgumentException("OBJ has no vertex lines: " + inputPath);
        }
        
        return bounds;
    }
    
    private static String centerVertexLine(String line, float centerX, float centerY, float centerZ) {
        String[] parts = line.trim().split("\\s+");
        
        float x = Float.parseFloat(parts[1]) - centerX;
        float y = Float.parseFloat(parts[2]) - centerY;
        float z = Float.parseFloat(parts[3]) - centerZ;
        
        return String.format(Locale.US, "v %.6f %.6f %.6f", x, y, z);
    }
    
    private static final class Bounds {
        private boolean hasVertex = false;
        
        private float minX = Float.POSITIVE_INFINITY;
        private float minY = Float.POSITIVE_INFINITY;
        private float minZ = Float.POSITIVE_INFINITY;
        
        private float maxX = Float.NEGATIVE_INFINITY;
        private float maxY = Float.NEGATIVE_INFINITY;
        private float maxZ = Float.NEGATIVE_INFINITY;
        
        private void include(float x, float y, float z) {
            hasVertex = true;
            
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;
            
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;
        }
    }
}
