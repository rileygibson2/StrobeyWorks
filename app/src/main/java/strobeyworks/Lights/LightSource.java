package strobeyworks.Lights;

import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NONE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_BORDER_COLOR;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawBuffer;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glReadBuffer;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameterfv;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;

import org.joml.Matrix4f;

import strobeyworks.SWMain;
import strobeyworks.object.SceneObject;
import strobeyworks.render.SceneRenderer;
import strobeyworks.utils.Vec3;

public abstract class LightSource {
    private Vec3 color;
    private float intensity;

    protected boolean shadowEnabled;
    protected int shadowFBO;
    protected int shadowDepthTexture;
    protected Matrix4f lightSpaceMatrix;

    protected SceneObject indicator;
    
    public LightSource(Vec3 color, float intensity) {
        this.color = color;
        this.intensity = intensity;
        this.shadowEnabled = true;
        this.shadowFBO = 0;
        this.shadowDepthTexture = 0;

        this.indicator = new SceneObject(((SceneRenderer) SWMain.getRenderWindow().getRenderer()).indicatorSphere);
    }

    protected abstract void calculateLightSpaceMatrix();
    
    public void initShadowMap(int width, int height) {
        shadowFBO = glGenFramebuffers();
        
        shadowDepthTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, shadowDepthTexture);
        
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_DEPTH_COMPONENT,
            width,
            height,
            0,
            GL_DEPTH_COMPONENT,
            GL_FLOAT,
            0
        );
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        
        float[] borderColor = {1f, 1f, 1f, 1f};
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor);
        
        glBindFramebuffer(GL_FRAMEBUFFER, shadowFBO);
        glFramebufferTexture2D(
            GL_FRAMEBUFFER,
            GL_DEPTH_ATTACHMENT,
            GL_TEXTURE_2D,
            shadowDepthTexture,
            0
        );
        
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Directional shadow framebuffer is not complete");
        }
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void enableShadow(boolean enabled) {this.shadowEnabled = enabled;}
    public boolean shadowEnabled() {return this.shadowEnabled;}

    public void setColor(Vec3 color) {this.color = color;}
    public void setRed(float red) {this.color = new Vec3(red, color.y, color.z);}
    public void setGreen(float green) {this.color = new Vec3(color.x, green, color.z);}
    public void setBlue(float blue) {this.color = new Vec3(color.x, color.y, blue);}


    public void setIntensity(float intensity) {this.intensity = intensity;}
    public void setShadowFBO(int fBO) {this.shadowFBO = fBO;}
    public void setShadowDepthTexture(int depthTexture) {this.shadowDepthTexture = depthTexture;}
    
    public Vec3 getColor() {return color;}
    public float getIntensity() {return intensity;}
    public int getShadowFBO() {return this.shadowFBO;}
    public int getShadowDepthTexture() {return this.shadowDepthTexture;}
    public Matrix4f getLightSpaceMatrix() {return this.lightSpaceMatrix;}
    public SceneObject getIndicator() {return this.indicator;}
}
