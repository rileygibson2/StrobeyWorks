package strobeyworks.pipeline.configs;

import strobeyworks.pipeline.RenderNode;

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
}
