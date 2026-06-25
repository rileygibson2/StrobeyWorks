package strobeyworks.ui.primitives;

import java.util.function.Supplier;

import strobeyworks.platform.ShaderManager;
import strobeyworks.ui.core.UIRenderer;

public class UITextureView extends UIElement {
    private Supplier<Integer> textureIdSupplier;
    
    public UITextureView(Supplier<Integer> textureIdSupplier) {
        this.textureIdSupplier = textureIdSupplier;
        style("box", UIBoxMode.FIXED);
    }
    
    public int getTextureId() {
        return textureIdSupplier.get();
    }
    
    @Override
    public void render(UIRenderer renderer, ShaderManager sM) {
        renderer.renderTextureView(sM, this);
    }
}
