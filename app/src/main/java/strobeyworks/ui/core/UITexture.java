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

import org.lwjgl.system.MemoryStack;

import strobeyworks.logger.Logger;
import strobeyworks.utils.Utils;

public class UITexture {
    
    private int textureId;
    private int width;
    private int height;
    
    public UITexture(String textureName) {
        loadTexture(textureName);
    }
    
    private void loadTexture(String textureName) {
        try {
            ByteBuffer imageBuffer = Utils.loadResourceToByteBuffer("/icons/"+textureName);
            
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(1);
                
                stbi_set_flip_vertically_on_load(false);
                
                ByteBuffer pixels = stbi_load_from_memory(imageBuffer, w, h, channels, 4);
                if (pixels == null) throw new RuntimeException(stbi_failure_reason());
                
                this.textureId = glGenTextures();
                this.width = w.get(0);
                this.height = h.get(0);
                
                glBindTexture(GL_TEXTURE_2D, this.textureId);
                
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
            }
        } catch (IOException e) {
            Logger.throwRuntimeException("Could not load texture: " + textureName, e);
        }
        
    }
    
    public int getTextureId() {
        return textureId;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}
