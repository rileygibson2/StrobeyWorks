package strobeyworks.pipeline.input;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

import strobeyworks.platform.ShaderManager;

public class RenderInputSlot {
    
    private final String uniformName;
    private RenderInput input;
    
    private boolean allowsTexture;
    
    public RenderInputSlot(String uniformName, boolean allowsTexture) {
        this.uniformName = uniformName;
        this.allowsTexture = allowsTexture;
    }
    
    public int upload(ShaderManager sM, int currentTextureUnit) {
        if (input==null) return currentTextureUnit;
        
        if (input instanceof FloatConstantInput f) {
            sM.setUniformFloat(uniformName+"Value", f.getBinding().getValue());
            sM.setUniformInt(uniformName+"SourceType", 0);
        }
        
        if (input instanceof BooleanConstantInput b) {
            sM.setUniformInt(uniformName+"Value", b.getBinding().getValue() ? 1 : 0);
            sM.setUniformInt(uniformName+"SourceType", 0);
        }

        if (input instanceof SelectConstantInput s) {
            sM.setUniformInt(uniformName+"Value", s.getBinding().getValue());
            sM.setUniformInt(uniformName+"SourceType", 0);
        }
        
        if (input instanceof TextureInput t) {
            glActiveTexture(GL_TEXTURE0+currentTextureUnit);
            glBindTexture(GL_TEXTURE_2D, t.getSourceNode().getRenderTarget().getTexture());
            
            sM.setUniformInt(uniformName+"SourceType", 1);
            sM.setUniformInt(uniformName+"Tex", currentTextureUnit);
            sM.setUniformInt(uniformName+"TexMode", t.getMode().ordinal());
            currentTextureUnit++;
        }
        
        return currentTextureUnit;
    }
    
    public RenderInput getInput() {
        return input;
    }
    
    public boolean setInput(RenderInput input) {
        if (input == null || !accepts(input)) return false;
        
        if (this.input instanceof ConstantInput<?> current
            && input instanceof ConstantInput<?> next
            && current.updateFrom(next)
        ) {
            return true;
        }
        
        this.input = input;
        return true;
    }
    
    public boolean hasInput() {
        return input!=null;
    }
    
    public boolean accepts(RenderInput input) {
        if (!allowsTexture&&input instanceof TextureInput) return false;
        return true;
    }
    
    public boolean allowsTexture() {
        return allowsTexture;
    }
    
    public String getUniformName() {
        return uniformName;
    }
}
