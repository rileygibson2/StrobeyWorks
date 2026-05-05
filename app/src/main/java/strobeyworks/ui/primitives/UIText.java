package strobeyworks.ui.primitives;

import static strobeyworks.ui.core.UIColors.col;

import strobeyworks.platform.ShaderManager;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UIPair;
import strobeyworks.utils.Vec4;

public class UIText extends UIElement {
    
    private String text;
    private UIFont font;
    private Vec4 color;
    
    public UIText(UIPair width, UIPair height, UIFont font, String text) {
        super(width, height);
        this.font = font;
        this.text = text;
        color = col(UIColors.TRANSPARENT);
    }
    
    public UIText() {
        super();
        color = col(UIColors.TRANSPARENT);
    }
    
    public void setRenderUniforms(ShaderManager sM) {
        sM.setUniformVec4("uColor", color);
        super.setRenderUniforms(sM);
    }
    
    public UIText color(Vec4 color) {
        this.color = color;
        return this;
    }
    
    public String getText() {
        return text;
    }
    
    public UIFont getFont() {
        return font;
    }
    
    public Vec4 getColor() {
        return color;
    }
    
}
