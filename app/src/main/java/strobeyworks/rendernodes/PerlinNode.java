package strobeyworks.rendernodes;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

import java.util.UUID;

import strobeyworks.SWMain;
import strobeyworks.pipeline.RenderNode;
import strobeyworks.pipeline.controls.ControlItem.ControlGroup;
import strobeyworks.pipeline.controls.ControlItem.ControlTab;
import strobeyworks.pipeline.input.BooleanConstantInput;
import strobeyworks.pipeline.input.FloatConstantInput;
import strobeyworks.pipeline.input.TextureInput;
import strobeyworks.platform.ShaderManager;
import strobeyworks.utils.Vec2I;
import strobeyworks.utils.Vec3;

public class PerlinNode extends RenderNode {
    
    private int noiseProgram;
    
    private Vec3 colorLow;
    private Vec3 colorHigh;
    
    public PerlinNode() {
        this(UUID.randomUUID());
    }
    
    public PerlinNode(UUID id) {
        super(id, "Perlin-Noise", "perlin", true);
        
        this.colorLow = new Vec3(0f, 0f, 0f);
        this.colorHigh = new Vec3(1f, 1f, 1f);
    }

    @Override
    protected void setupFeeds() {}
    
    @Override
    protected void setupParameters() {
        ControlTab t = createControlTab("Structure");
        ControlGroup g = createControlGroup("Base", t);
        
        floatParam(g, "Speed", "uSpeed", 0f, 1f, 3, 0.05f, 0.5f, true);
        floatParam(g, "Grid", "uGridSize", 2f, 50f, 0, 1f, 10f, true);
        floatParam(g, "Octaves", "uOctaves", 1f, 10f, 0, 1f, 1f, true);
        floatParam(g, "Gamma", "uGamma", 0f, 5f, 3, 0.1f, 4f, true);
        floatParam(g, "Gain", "uGain", 0f, 5f, 3, 0.1f, 4f, true);
        
        g = createControlGroup("Seed", t);
        
        floatParam(g, "Offset", "uSeedOffset", 0f, 100f, 0, 1f, 0f, true);
        floatParam(g, "Salt", "uSalt", 0f, 9999f, 0, 1f, 0f, false);
        
        t = createControlTab("Processing");
        g = createControlGroup("Warp", t);
        
        boolParam(g, "Warp", "uWarp", false);
        floatParam(g, "Strength", "uWarpStrength", 0f, 3f, 3, 0.1f, 0f, true);
        floatParam(g, "Scale", "uWarpScale", 0f, 3f, 3, 0.1f, 1f, true);
        
        g = createControlGroup("Ridge", t);
        
        boolParam(g, "Per Octave", "uOctaveRidge", false);
        boolParam(g, "Post", "uPostRidge", false);
        floatParam(g, "Ridge Power", "uRidgePow", 0f, 5f, 3, 0.1f, 2f, true);
        
        g = createControlGroup("Turbulence", t);
        
        boolParam(g, "Octave Turbulence", "uOctaveTurbulence", false);
        floatParam(g, "Power", "uTurbulencePow", 0f, 5f, 3, 0.1f, 2f, true);
    }
    
    public void setColorHigh(Vec3 color) {
        this.colorHigh = color;
    }

    @Override
    public void feedInputsChanged() {}
    
    @Override
    public void initialise(Vec2I dimensions) {
        super.initialise(dimensions);
        
        ShaderManager sM = ShaderManager.getInstance();
        
        // Shaders init
        noiseProgram = sM.createProgram("noise/perlinfbm.vert", "noise/perlinfbm.frag");
        initialiseFullQuad();
        
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
        
        sM.setUniformFloat("uTime", SWMain.getTotalTime());
        sM.setUniformFloat("uLacunarity", 2.0f);
        sM.setUniformFloat("uPersistence", 0.5f);
        
        sM.setUniformVec3("uColorLow", colorLow);
        sM.setUniformVec3("uColorHigh", colorHigh);
        
        uploadInputs(sM);
        bindAndDrawFullScreen();
        
        // Reset
        glActiveTexture(GL_TEXTURE0);
        sM.bindVAO(0);
        sM.useProgram(0);
    }
    
    @Override
    protected void handleCleanup() {
        if (noiseProgram!=0) {
            glDeleteProgram(noiseProgram);
            noiseProgram = 0;
        }
    }
}
