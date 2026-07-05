package strobeyworks.pipeline.input;

import strobeyworks.pipeline.RenderNode;
import strobeyworks.platform.ShaderManager;

public class TextureInput extends RenderInput {
    
    public record TextureInputState(
        String nodeID,
        String mode
    ) {}
    
    public enum TextureInputMode {
        RED("r", "red"),
        GREEN("g", "green"),
        BLUE("b", "blue"),
        ALPHA("a", "alpha"),
        LUMINANCE("luma", "lum", "luminance", "brightness"),
        UNNECESSARY("none", "all", "rgba", "color");
        
        private final String[] aliases;
        
        TextureInputMode(String... aliases) {
            this.aliases = aliases;
        }
        
        public static TextureInputMode fromString(String text) {
            String cleaned = text.strip().toLowerCase();
            for (TextureInputMode mode : values()) {
                if (mode.name().equalsIgnoreCase(cleaned)) return mode;
                for (String alias : mode.aliases) if (alias.equals(cleaned)) return mode;
            }
            return null;
        }
    }
    
    private RenderNode sourceNode;
    private TextureInputMode mode;
    
    public TextureInput(RenderNode sourceNode) {
        this(sourceNode, TextureInputMode.UNNECESSARY);
    }
    
    public TextureInput(RenderNode sourceNode, TextureInputMode mode) {
        super(true);
        this.sourceNode = sourceNode;
        this.mode = mode;
    }
    
    public RenderNode getSourceNode() {
        return sourceNode;
    }
    
    public TextureInputMode getMode() {
        return mode;
    }
    
    public void upload(ShaderManager sM) {}
    
    @Override
    public RenderInputState getState() {
        return new RenderInputState(
            "float",
            null,
            sourceNode.getIDString(),
            mode.name()
        );
    }
    
    @Override
    public String getString() {
        return sourceNode.getCustomName()+"."+mode.aliases[1];
    }
}
