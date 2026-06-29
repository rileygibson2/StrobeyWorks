package strobeyworks.pipeline.input;

import strobeyworks.pipeline.RenderNode;
import strobeyworks.pipeline.RenderPipeline;
import strobeyworks.pipeline.input.TextureInput.TextureInputMode;

public final class RenderInputParser {
    
    
    public static RenderInput parse(String input, String uniformName) {
        input = clean(input);
        
        ConstantInput<?> numConst = tryNumericConstant(input);
        if (numConst!=null) {
            numConst.setUniformName(uniformName);
            return numConst;
        }

        TextureInput tex = tryTexture(input);
        if (tex!=null) {
            tex.setUniformName(uniformName);
            return tex;
        }
        
        return null;
    }
    
    private static String clean(String input) {
        return input.strip().toUpperCase();
    }
    
    private static ConstantInput<?> tryNumericConstant(String input) {
        try { // Float
            float value = Float.parseFloat(input);
            return new FloatConstantInput(null, value);
        } catch (NumberFormatException e) {return null;}
    }
    
    private static TextureInput tryTexture(String input) {
        String[] parts = input.split("\\.");
        if (parts.length!=2) return null;
        
        // Try get node
        RenderNode target = null;
        for (RenderNode node : RenderPipeline.getInstance().getAllNodes()) {
            if (node.getCustomName().toUpperCase().equals(parts[0])) target = node;
        }
        if (target==null||!target.hasTextureOutput()) return null;
        
        // Try get texture mode
        TextureInputMode mode = TextureInputMode.fromString(parts[1]);
        if (mode==null) return null;
        
        return new TextureInput(target, mode);
    }
}
