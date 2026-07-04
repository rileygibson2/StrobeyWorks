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

import strobeyworks.pipeline.RenderNode;
import strobeyworks.pipeline.controls.ControlItem.ControlGroup;
import strobeyworks.pipeline.controls.ControlItem.ControlTab;
import strobeyworks.platform.ShaderManager;
import strobeyworks.utils.Vec2I;

public class PixelDots extends RenderNode {
    
    private int program;
    
    public PixelDots() {
        this(UUID.randomUUID());
    }
    
    public PixelDots(UUID id) {
        super(id, "Pixel Dots", "pixdots", true);
    }
    
    @Override
    protected void setupFeeds() {
        createFeedSlot("uMainFeed", true);
    }
    
    @Override
    protected void setupParameters() {
        ControlTab t = createControlTab("Pixel Dots");
        ControlGroup g = createControlGroup("Pixel Dots", t);
        
        floatParam(g, "Grid", "uGridSize", false, 2f, 200f, 0, 1f, 20f, true);
        floatParam(g, "Dot Scale", "uDotScale", false, 0f, 1f, 2, 0.01f, 1f, true);
        boolParam(g, "Invert", "uInvert", false);
    }
    
    @Override
    public void feedInputsChanged() {}
    
    @Override
    public void initialise(Vec2I dimensions) {
        super.initialise(dimensions);
        
        ShaderManager sM = ShaderManager.getInstance();
        
        // Shaders init
        program = sM.createProgram("default_FSQ.vert", "pixelate/pixeldots.frag");
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
        sM.useProgram(program);
        sM.setCurrentProgram(program);
        
        uploadInputs(sM);
        
        bindAndDrawFullScreen();
        
        // Reset
        glActiveTexture(GL_TEXTURE0);
        sM.bindVAO(0);
        sM.useProgram(0);
    }
    
    @Override
    protected void handleCleanup() {
        if (program!=0) glDeleteProgram(program);
    }
}
