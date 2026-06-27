package strobeyworks.pipeline;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDrawBuffer;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;

import strobeyworks.logger.Logger;
import strobeyworks.utils.Vec2I;

public class RenderTarget {
    
    private final int fbo;
    private final int texture;
    private Vec2I dimensions;
    
    private RenderTarget(int fbo, int texture, Vec2I dimensions) {
        this.fbo = fbo;
        this.texture = texture;
        this.dimensions = Vec2I.of(dimensions);
    }
    
    public static RenderTarget texture(Vec2I dimensions) {
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA,
            dimensions.x,
            dimensions.y,
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            0
        );
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        
        int fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        
        glFramebufferTexture2D(
            GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0,
            GL_TEXTURE_2D,
            texture,
            0
        );
        
        glDrawBuffer(GL_COLOR_ATTACHMENT0);
        
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("RenderTarget framebuffer is incomplete");
        }
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        
        return new RenderTarget(fbo, texture, dimensions);
    }
    
    public void resize(Vec2I dimensions) {
        this. dimensions = dimensions;
        
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA,
            dimensions.x,
            dimensions.y,
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            0
        );
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    
    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glViewport(0, 0, dimensions.x, dimensions.y);
    }
    
    public void cleanup() {   
        Logger.info("Cleaning up");     
        glDeleteFramebuffers(fbo);
        glDeleteTextures(texture);
    }
    
    public int getFBO() {
        return fbo;
    }
    
    public int getTexture() {
        return texture;
    }
    
    public Vec2I getDimensions() {
        return dimensions;
    }
}