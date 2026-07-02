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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import strobeyworks.pipeline.RenderNode;
import strobeyworks.pipeline.controls.ControlItem.ControlGroup;
import strobeyworks.pipeline.controls.ControlItem.ControlTab;
import strobeyworks.platform.ShaderManager;
import strobeyworks.utils.BindableValue;
import strobeyworks.utils.Vec2I;

public class MixNode extends RenderNode {
    
    private int program;
    
    private ControlGroup weightGroup;
    private List<BindableValue<Float>> weights;
    
    public MixNode() {
        this(UUID.randomUUID());
    }
    
    public MixNode(UUID id) {
        super(id, "Mix", "mix", true);
        weights = new ArrayList<>();
    }
    
    @Override
    protected void setupFeeds() {
        for (int i=0; i<5; i++) {
            createFeedSlot("uMainFeed"+i, true);
        }
    }
    
    @Override
    protected void setupParameters() {
        ControlTab t = createControlTab("Mix");
        weightGroup = createControlGroup("Weights", t);
    }
    
    @Override
    public void initialise(Vec2I dimensions) {
        super.initialise(dimensions);
        
        ShaderManager sM = ShaderManager.getInstance();
        
        // Shaders init
        program = sM.createProgram("mix/mix.vert", "mix/mix.frag");
        initialiseFullQuad();
        sM.useProgram(0);
    }
    
    @Override
    public void feedInputsChanged() {}
    
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
        
        for (int i = 0; i < weights.size(); i++) {
            sM.setUniformFloat("uMixWeights[" + i + "]", weights.get(i).getValue());
        }
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
