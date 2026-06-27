package strobeyworks.pipeline.configs;

import strobeyworks.pipeline.RenderNode;
import strobeyworks.pipeline.RenderNode.RenderInputState;

public final record RenderInputConfig(
    RenderNode node,
    TextureInputMode mode,
    int inputInsight
) {
    
    public enum TextureInputMode {
        RED,
        GREEN,
        BLUE,
        ALPHA,
        LUMINANCE,
        UNNECESSARY
    }

    public RenderInputConfig(RenderNode node) {this(node, TextureInputMode.UNNECESSARY, 0);}

    public RenderInputState getState() {
        return new RenderInputState(node.getIDString());
    }
}
