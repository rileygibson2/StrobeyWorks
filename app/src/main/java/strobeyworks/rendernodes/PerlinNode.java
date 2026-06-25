package strobeyworks.rendernodes;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.util.ArrayList;

import strobeyworks.SWMain;
import strobeyworks.pipeline.RenderNode;
import strobeyworks.pipeline.InspectorItem.InspectorGroup;
import strobeyworks.pipeline.InspectorItem.InspectorTab;
import strobeyworks.platform.ShaderManager;
import strobeyworks.utils.BindableValue;
import strobeyworks.utils.BindableValueObserver;
import strobeyworks.utils.Utils;
import strobeyworks.utils.Vec3;

public class PerlinNode extends RenderNode {
    
    private int noiseProgram;
    private int quadVAO;
    private int quadVBO;
    
    private BindableValue<Float> speed;
    private BindableValue<Float> gridSize;
    
    private BindableValue<Float> octaves;
    
    private BindableValue<Float> gamma;
    private BindableValue<Float> gain;
    
    private BindableValue<Boolean> warp;
    private BindableValue<Float> warpStrength;
    private BindableValue<Float> warpScale;
    
    private BindableValue<Boolean> octaveRidge;
    private BindableValue<Boolean> postRidge;
    private BindableValue<Float> ridgePow;
    
    private BindableValue<Boolean> octaveTurbulence;
    private BindableValue<Float> turbulencePow;
    
    private Vec3 colorLow;
    private Vec3 colorHigh;
    
    public PerlinNode() {
        super("Perlin-Noise", "perlin");
        
        this.colorLow = new Vec3(0f, 0f, 0f);
        this.colorHigh = new Vec3(1f, 0f, 0.8f);
    }
    
    @Override
    protected void setupControls() {
        super.setupControls();
          
        createInspectorTab("Structure");
        createInspectorGroup("Base");
        speed = addFloatControl("Speed", 0f, 1f, 3, 0.05f, 0.5f);
        gridSize = addFloatControl("Grid", 2f, 50f, 0, 1f, 10f);
        octaves = addFloatControl("Octaves", 1f, 10f, 0, 1f, 1f);
        gamma = addFloatControl("Gamma", 0f, 5f, 3, 0.1f, 1f);
        gain = addFloatControl("Gain", 0f, 5f, 3, 0.1f, 1f);
        
        createInspectorTab("Processing");
        createInspectorGroup("Warp");
        warp = addBooleanControl("Warp", false);
        warpStrength = addFloatControl("Strength", 0f, 3f, 3, 0.1f, 0f);
        warpScale = addFloatControl("Scale", 0f, 3f, 3, 0.1f, 1f);
        
        createInspectorGroup("Ridge");
        octaveRidge = addBooleanControl("Per Octave", false);
        postRidge = addBooleanControl("Post", false);
        ridgePow = addFloatControl("Ridge Power", 0f, 5f, 3, 0.1f, 2f);

        createInspectorGroup("Turbulence");
        octaveTurbulence = addBooleanControl("Octave Turbulence", false);
        turbulencePow = addFloatControl("Power", 0f, 5f, 3, 0.1f, 2f);
    }
    
    @Override
    public void initialise(int outputWidth, int outputHeight) {
        super.initialise(outputWidth, outputHeight);
        
        ShaderManager sM = ShaderManager.getInstance();
        
        // Shaders init
        noiseProgram = sM.createProgram("noise/perlinfbm.vert", "noise/perlinfbm.frag");
        quadVAO = glGenVertexArrays();
        quadVBO = glGenBuffers();
        sM.bindVAO(quadVAO);
        sM.bindVBO(quadVBO);
        glBufferData(GL_ARRAY_BUFFER, ShaderManager.QUAD_VERTICES, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        sM.bindVAO(0);
        sM.bindVBO(0);
        sM.useProgram(0);
    }
    
    @Override
    public void render() {
        getRenderTarget().bind();
        
        glDisable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);
        
        renderPass();
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    
    private void renderPass() {
        ShaderManager sM = ShaderManager.getInstance();
        sM.useProgram(noiseProgram);
        sM.setCurrentProgram(noiseProgram);
        
        double gridSizeV = Math.ceil(gridSize.getValue());
        gridSizeV = Utils.clamp(2, 1000, gridSizeV);
        sM.setUniformInt("uGridSize", (int) gridSizeV);
        sM.setUniformFloat("uTime", SWMain.getTotalTime());
        sM.setUniformFloat("uSpeed", speed.getValue());
        
        double octaveV = Math.ceil(octaves.getValue());
        octaveV = Utils.clamp(1, 10, octaveV);
        sM.setUniformInt("uOctaves", (int) octaveV);
        sM.setUniformFloat("uLacunarity", 2.0f);
        sM.setUniformFloat("uPersistence", 0.5f);
        
        sM.setUniformFloat("uGamma", gamma.getValue());
        sM.setUniformFloat("uGain", gain.getValue());
        
        sM.setUniformInt("uWarp", warp.getValue() ? 1 : 0);
        sM.setUniformFloat("uWarpStrength", warpStrength.getValue());
        sM.setUniformFloat("uWarpScale", warpScale.getValue());
        
        sM.setUniformInt("uOctaveRidge", octaveRidge.getValue() ? 1 : 0);
        sM.setUniformInt("uPostRidge", postRidge.getValue() ? 1 : 0);
        sM.setUniformFloat("uRidgePow", ridgePow.getValue());
        
        sM.setUniformInt("uOctaveTurbulence", octaveTurbulence.getValue() ? 1 : 0);
        sM.setUniformFloat("uTurbulencePow", turbulencePow.getValue());
        
        sM.setUniformVec3("uColorLow", colorLow);
        sM.setUniformVec3("uColorHigh", colorHigh);
        
        sM.bindVAO(quadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        
        // Reset
        glActiveTexture(GL_TEXTURE0);
        sM.bindVAO(0);
        sM.useProgram(0);
    }
    
    @Override
    protected void handleCleanup() {
        if (quadVAO!=0) {
            glDeleteVertexArrays(quadVAO);
            quadVAO = 0;
        }
        
        if (quadVBO!=0) {
            glDeleteBuffers(quadVBO);
            quadVBO = 0;
        }
        
        if (noiseProgram!=0) {
            glDeleteProgram(noiseProgram);
            noiseProgram = 0;
        }
    }
}
