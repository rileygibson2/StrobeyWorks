package strobeyworks.ui.primitives;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.logger.Logger;
import strobeyworks.platform.ShaderManager;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.style.PrimitiveStyles;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.utils.Vec4;

public class UIText extends UIElement {
    
    private String text;
    private float resolvedTextWidth;
    private float resolvedTextHeight;
    
    private UIFont font;
    private Vec4 color;
    
    public UIText(UIFont font) {
        super(px(0), px(0));
        this.font = font;
        this.text = "";
        color = col(UIColors.WHITE);
        
        box(UIBoxMode.FIXED);
        resolveTextBounds();
    }
    
    public UIText(UIFont font, String text) {
        super(px(0), px(0));
        this.font = font;
        this.text = text!=null ? text : "";
        color = col(UIColors.WHITE);
        
        box(UIBoxMode.FIXED);
        resolveTextBounds();
    }
    
    public void setRenderUniforms(ShaderManager sM) {
        sM.setUniformVec4("uColor", color);
        super.setRenderUniforms(sM);
    }
    
    @Override
    public void applyStyle(UIStyle style) {
        super.applyStyle(style);
        
        style.ifPresent(PrimitiveStyles.FONT, this::font);
        style.ifPresent(PrimitiveStyles.COLOR, this::color);
    }
    
    @Override
    public UIStyle captureStyle() {
        UIStyle style = super.captureStyle();
        
        style.set(PrimitiveStyles.FONT, font);
        style.set(PrimitiveStyles.COLOR, color);
        return style;
    }
    
    private void resolveTextBounds() {
        this.resolvedTextWidth = font.measureTextWidth(text);
        this.resolvedTextHeight = font.measureTextHeight(text);
        markLayoutDirty();
    }
    
    @Override
    public UIElement box(UIBoxMode boxMode) {
        if (boxMode!=UIBoxMode.FIXED) Logger.throwRuntimeException("Cannot set UIText to any box mode other than UIBoxMode.FIXED");
        return super.box(boxMode);
    }
    
    public UIText setText(String text) {
        if (text==null||this.text.equals(text)) return this;
        this.text = text;
        resolveTextBounds();
        return this;
    }
    
    public UIText color(Vec4 color) {
        this.color = color;
        return this;
    }

    public UIText font(UIFont font) {
        this.font = font;
        return this;
    }
    
    public String getText() {
        return text;
    }
    
    public UIFont getFont() {
        return font;
    }
    
    public float getFontSize() {
        return font.getFontSize();
    }
    
    public Vec4 getColor() {
        return color;
    }
    
    public float getResolvedTextWidth() {
        return this.resolvedTextWidth;
    }
    
    public float getResolvedTextHeight() {
        return this.resolvedTextHeight;
    }
}
