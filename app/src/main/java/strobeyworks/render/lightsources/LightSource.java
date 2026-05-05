package strobeyworks.render.lightsources;

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
import strobeyworks.utils.Bindable;
import strobeyworks.utils.Vec3;

public abstract class LightSource {
    
    private Bindable<Float> intensity;
    private Bindable<Float> red;
    private Bindable<Float> green;
    private Bindable<Float> blue;
    private Bindable<Boolean> shadowEnabled;

    protected int shadowFBO;
    protected int shadowDepthTexture;
    protected Matrix4f lightSpaceMatrix;

    protected SceneObject indicator;

    public LightSource() {
        this.intensity = Bindable.of(1f);
        this.red = Bindable.of(1f);
        this.green = Bindable.of(1f);
        this.blue = Bindable.of(1f);
        this.shadowEnabled = Bindable.of(true);
        
        this.shadowFBO = 0;
        this.shadowDepthTexture = 0;

        this.indicator = new SceneObject(((SceneRenderer) SWMain.getRenderWindow().getRenderer()).indicatorSphere);
    }
    
    public LightSource(Vec3 color, float intensity) {
        this.intensity = Bindable.of(intensity);
        this.red = Bindable.of(color.x);
        this.green = Bindable.of(color.y);
        this.blue = Bindable.of(color.z);
        this.shadowEnabled = Bindable.of(true);

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

    public void enableShadow(boolean shadowEnabled) {this.shadowEnabled.setValue(shadowEnabled);}

    public boolean shadowEnabled() {return this.shadowEnabled.getValue();}

    public void setIntensity(float intensity) {
        this.intensity.setValue(intensity);
    }

    public void setColor(Vec3 color) {
        red.setValue(color.x);
        green.setValue(color.y);
        blue.setValue(color.z);
    }

    public void setRed(float red) {
        this.red.setValue(red);
    }

    public void setGreen(float green) {
        this.green.setValue(green);
    }

    public void setBlue(float blue) {
        this.blue.setValue(blue);
    }

    public void setShadowFBO(int fBO) {this.shadowFBO = fBO;}

    public void setShadowDepthTexture(int depthTexture) {this.shadowDepthTexture = depthTexture;}
    
    public Vec3 getColorVec3() {return new Vec3(red.getValue(), green.getValue(), blue.getValue());}

    public int getShadowFBO() {return this.shadowFBO;}

    public int getShadowDepthTexture() {return this.shadowDepthTexture;}

    public Matrix4f getLightSpaceMatrix() {return this.lightSpaceMatrix;}

    public SceneObject getIndicator() {return this.indicator;}

    public Bindable<Float> getIntensity() {return this.intensity;}

    public Bindable<Float> getRed() {return this.red;}

    public Bindable<Float> getGreen() {return this.green;}

    public Bindable<Float> getBlue() {return this.blue;}

    public Bindable<Boolean> getShadowEnabled() {return this.shadowEnabled;}
}
