package strobeyworks.ui.primitives;

import static strobeyworks.ui.core.UIColors.col;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.core.UITexture;
import strobeyworks.ui.core.UITextureManager;
import strobeyworks.ui.style.StyleProps;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.ui.style.UIStyleProperty;
import strobeyworks.utils.Vec4;

public class UIIcon extends UIElement {
    
    private static final Map<UIStyleProperty<?>, BiConsumer<UIIcon, Object>> APPLIERS = new HashMap<>();
    
    static {
        register(APPLIERS, StyleProps.TINT, UIIcon::tint);
    }
    
    private Vec4 uvRect = new Vec4(0f, 0f, 1f, 1f);
    
    private UITexture texture;
    private Vec4 tint = col(UIColors.WHITE);
    
    public UIIcon(String textureName) {
        super();
        this.texture = UITextureManager.getUITexture(textureName);
        
        style("border-color", col(UIColors.WHITE));
        style("box", UIBoxMode.FIXED);
    }
    
    public UIIcon() {
        super();
        
        style("border-color", col(UIColors.WHITE));
        style("box", UIBoxMode.FIXED);
    }
    
    @Override
    protected void applyStyleProperty(UIStyleProperty<?> property, Object value) {
        BiConsumer<UIIcon, Object> applier = APPLIERS.get(property);
        if (applier!=null) applier.accept(this, value);
        else super.applyStyleProperty(property, value);
    }
    
    @Override
    public UIStyle captureStyle() {
        UIStyle style = super.captureStyle();
        
        style.set(StyleProps.TINT, tint);
        return style;
    }
    
    public UIIcon texture(String textureName) {
        this.texture = UITextureManager.getUITexture(textureName);
        return this;
    }
    
    private UIIcon tint(Vec4 tint) {
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

