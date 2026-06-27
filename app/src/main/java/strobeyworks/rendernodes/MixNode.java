package strobeyworks.rendernodes;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
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
import strobeyworks.pipeline.RenderPipeline;
import strobeyworks.pipeline.ControlItem.ControlGroup;
import strobeyworks.pipeline.configs.RenderInputConfig;
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
        super(id, "Mix", "mix");
        weights = new ArrayList<>();
    }
    
    @Override
    protected void setupControls() {
        createControlTab("Mix");
        weightGroup = createControlGroup("Weights");
        super.setupControls();
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
    protected void renderInputAdded(RenderInputConfig config) {
        float d = 0.5f;
        BindableValue<Float> w = addFloatControl("Input "+(getInputNodeCount()), 0f, 1f, 2, 0.1f, d, true, weightGroup);
        weights.add(w);

        RenderPipeline.getInstance().handleNodeControlsChanged();
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
        
        List<RenderInputConfig> inputConfigs = getInputConfigs();

        for (int i=0; i<inputConfigs.size(); i++) {
            RenderInputConfig config = inputConfigs.get(i);
            
            glActiveTexture(GL_TEXTURE0+i);
            glBindTexture(GL_TEXTURE_2D, config.node().getRenderTarget().getTexture());
            
            sM.setUniformInt("uInputTextures["+i+"]", i);
            sM.setUniformInt("uInputModes["+i+"]", config.mode().ordinal());
            
            sM.setUniformFloat("uMixWeights["+i+"]", weights.get(i).getValue());
        }
        sM.setUniformInt("uInputCount", getInputNodeCount());

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
