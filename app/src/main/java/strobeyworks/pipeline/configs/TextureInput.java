package strobeyworks.pipeline.configs;

import strobeyworks.pipeline.RenderNode;

public class TextureInput {

    public record TextureInputState(
        String nodeID,
        String mode
    ) {}
    
    public enum TextureInputMode {
        RED,
        GREEN,
        BLUE,
        ALPHA,
        LUMINANCE,
        UNNECESSARY
    }

    private RenderNode node;
    private TextureInputMode mode;

    public TextureInput(RenderNode node) {
        this(node, TextureInputMode.UNNECESSARY);
    }

    public TextureInput(RenderNode node, TextureInputMode mode) {
        this.node = node;
        this.mode = mode;
    }

    public RenderNode getNode() {
        return node;
    }

    public TextureInputMode getMode() {
        return mode;
    }

    public TextureInputState getState() {
        return new TextureInputState(node.getIDString(), mode.name());
    }
}
