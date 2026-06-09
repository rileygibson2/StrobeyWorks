package strobeyworks.ui.primitives;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import strobeyworks.platform.ShaderManager;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIRenderer;
import strobeyworks.ui.core.UITexture;
import strobeyworks.ui.core.UITextureManager;
import strobeyworks.ui.style.StyleProps;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.ui.style.UIStyleProperty;
import strobeyworks.utils.Vec4;

public class UIIcon extends UIElement {
    
    /**
    * Sets how icons will be drawn in the surronding UIIcon bounds.
    */
    public enum UIIconFitMode {
        STRETCH,
        FIT
    }

    private static final Map<UIStyleProperty<?>, BiConsumer<UIIcon, Object>> APPLIERS = new HashMap<>();
    
    static {
        register(APPLIERS, StyleProps.TINT, UIIcon::tint);
        register(APPLIERS, StyleProps.ICON_FIT_MODE, UIIcon::iconFitMode);
    }
    
    private Vec4 uvRect = new Vec4(0f, 0f, 1f, 1f);
    
    private UITexture texture;
    private UIColor tint;
    private UIIconFitMode fitMode;
    
    public UIIcon(String textureName) {
        super();
        this.texture = UITextureManager.getUITexture(textureName);
        
        style("border-color", UIColor.white());
        style("box", UIBoxMode.FIXED);
        style("tint", UIColor.white());
        style("icon-fit-mode", UIIconFitMode.FIT);
    }
    
    public UIIcon() {
        super();
        
        style("border-color", UIColor.white());
        style("box", UIBoxMode.FIXED);
        style("tint", UIColor.white());
        style("icon-fit-mode", UIIconFitMode.FIT);
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
        style.set(StyleProps.ICON_FIT_MODE, fitMode);
        return style;
    }
    
    public UIIcon texture(String textureName) {
        this.texture = UITextureManager.getUITexture(textureName);
        return this;
    }
    
    private UIIcon tint(UIColor tint) {
        this.tint = tint;
        return this;
    }

    private UIIcon iconFitMode(UIIconFitMode fitMode) {
        this.fitMode = fitMode;
        return this;
    }

    public UIIconFitMode getFitMode() {
        return fitMode;
    }

    public UITexture getTexture() {
        return this.texture;
    }
    
    public int getTextureId() {
        return texture.getTextureId();
    }
    
    public UIColor getTint() {
        return tint;
    }
    
    public Vec4 getUVRect() {
        return uvRect;
    }

    @Override
    public void render(UIRenderer renderer, ShaderManager sM) {
        renderer.renderIcon(sM, this);
    }
}

