package strobeyworks.ui.core;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.system.MemoryStack;

import strobeyworks.logger.Logger;
import strobeyworks.utils.Utils;

public class UITextureManager {
    
    private static final Map<String, UITexture> loaded = new HashMap<>();
    
    public static UITexture getUITexture(String textureName) {
        // Check is a filename, if not default to .png
        String filename = textureName.matches(".*\\.[A-Za-z0-9]+$")
        ? textureName : textureName + ".png";

        UITexture tex = loaded.get(filename);
        if (tex==null) tex = loadTexture(filename);
        if (tex==null) Logger.throwRuntimeException("Could not load texture: " + filename);
        
        return tex;
    }
    
    public static UITexture loadTexture(String fileName) {
        try {
            ByteBuffer imageBuffer = Utils.loadResourceToByteBuffer("/icons/"+fileName);
            
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(1);
                
                stbi_set_flip_vertically_on_load(false);
                
                ByteBuffer pixels = stbi_load_from_memory(imageBuffer, w, h, channels, 4);
                if (pixels == null) throw new RuntimeException(stbi_failure_reason());
                
                int textureId = glGenTextures();
                int width = w.get(0);
                int height = h.get(0);
                
                glBindTexture(GL_TEXTURE_2D, textureId);
                
                glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
                
                glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA,
                    width,
                    height,
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    pixels
                );
                
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
                
                glBindTexture(GL_TEXTURE_2D, 0);
                stbi_image_free(pixels);
                
                UITexture tex = new UITexture(fileName, textureId, width, height);
                loaded.put(fileName, tex);
                return tex;
            }
        } catch (IOException e) {
            Logger.throwRuntimeException("Could not load texture: " + fileName, e);
        }
        
        return null;
    }
}
