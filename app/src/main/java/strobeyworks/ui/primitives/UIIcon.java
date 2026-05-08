package strobeyworks.ui.primitives;

import static strobeyworks.ui.core.UIColors.col;

import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.core.UITexture;
import strobeyworks.ui.core.UITextureManager;
import strobeyworks.ui.style.PrimitiveStyles;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.utils.Vec4;

public class UIIcon extends UIElement {
    
    private Vec4 uvRect = new Vec4(0f, 0f, 1f, 1f);
    
    private UITexture texture;
    private Vec4 tint = col(UIColors.WHITE);
    
    public UIIcon(UILength width, UILength height, String textureName) {
        super(width, height);
        this.texture = UITextureManager.getUITexture(textureName);
        
        box(UIBoxMode.FIXED);
    }

    public UIIcon(UILength width, UILength height) {
        super(width, height);
        
        box(UIBoxMode.FIXED);
    }

    @Override
    public void applyStyle(UIStyle style) {
        super.applyStyle(style);

        style.ifPresent(PrimitiveStyles.TINT, this::tint);
    }

    @Override
    public UIStyle captureStyle() {
        UIStyle style = super.captureStyle();

        style.set(PrimitiveStyles.TINT, tint);
        return style;
    }
    
    public UIIcon texture(String textureName) {
        this.texture = UITextureManager.getUITexture(textureName);
        return this;
    }
    
    public UIIcon tint(Vec4 tint) {
        this.tint = tint;
        return this;
    }
    
    public int getTextureId() {
        return texture.getTextureId();
    }
    
    public Vec4 getTint() {
        return tint;
    }
    
    public Vec4 getUVRect() {
        return uvRect;
    }
}

