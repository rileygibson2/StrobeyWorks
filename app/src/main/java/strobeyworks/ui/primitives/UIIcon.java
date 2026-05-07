package strobeyworks.ui.primitives;

import static strobeyworks.ui.core.UIColors.col;

import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIPair;
import strobeyworks.ui.core.UITexture;
import strobeyworks.ui.core.UITextureManager;
import strobeyworks.utils.Vec4;

public class UIIcon extends UIElement {
    
    private Vec4 uvRect = new Vec4(0f, 0f, 1f, 1f);
    
    private UITexture texture;
    private Vec4 tint = col(UIColors.WHITE);
    
    public UIIcon(UIPair width, UIPair height, String textureName) {
        super(width, height);
        this.texture = UITextureManager.getUITexture(textureName);
        
        box(UIBoxMode.FIXED);
    }

    public UIIcon(UIPair width, UIPair height) {
        super(width, height);
        
        box(UIBoxMode.FIXED);
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

