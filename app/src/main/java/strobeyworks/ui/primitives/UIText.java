package strobeyworks.ui.primitives;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.px;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import strobeyworks.logger.Logger;
import strobeyworks.platform.ShaderManager;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.style.StyleProps;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.ui.style.UIStyleProperty;
import strobeyworks.utils.Vec4;

public class UIText extends UIElement {
    
    private static final Map<UIStyleProperty<?>, BiConsumer<UIText, Object>> APPLIERS = new HashMap<>();

    static {
        register(APPLIERS, StyleProps.COLOR, UIText::color);
        register(APPLIERS, StyleProps.FONT, UIText::font);
    }

    private String text;
    private float resolvedTextWidth;
    private float resolvedTextHeight;
    
    private UIFont font;
    private Vec4 color;
    
    public UIText(UIFont font) {
        super();
        this.font = font;
        this.text = "";

        style("color", col(UIColors.WHITE));
        style("box", UIBoxMode.FIXED);
        resolveTextBounds();
    }
    
    public UIText(UIFont font, String text) {
        super();
        this.font = font;
        this.text = text!=null ? text : "";
        
        style("color", col(UIColors.WHITE));
        style("box", UIBoxMode.FIXED);
        resolveTextBounds();
    }
    
    public void setRenderUniforms(ShaderManager sM) {
        sM.setUniformVec4("uColor", color);
        super.setRenderUniforms(sM);
    }
    
    @Override
    protected void applyStyleProperty(UIStyleProperty<?> property, Object value) {
        BiConsumer<UIText, Object> applier = APPLIERS.get(property);
        if (applier!=null) applier.accept(this, value);
        else super.applyStyleProperty(property, value);
    }
    
    @Override
    public UIStyle captureStyle() {
        UIStyle style = super.captureStyle();
        
        style.set(StyleProps.FONT, font);
        style.set(StyleProps.COLOR, color);
        return style;
    }
    
    private void resolveTextBounds() {
        this.resolvedTextWidth = font.measureTextWidth(text);
        this.resolvedTextHeight = font.measureTextHeight(text);
        markLayoutDirty();
    }
    
    @Override
    protected UIElement box(UIBoxMode boxMode) {
        if (boxMode!=UIBoxMode.FIXED) Logger.throwRuntimeException("Cannot set UIText to any box mode other than UIBoxMode.FIXED");
        return super.box(boxMode);
    }
    
    public UIText setText(String text) {
        if (text==null||this.text.equals(text)) return this;
        this.text = text;
        resolveTextBounds();
        return this;
    }
    
    private UIText color(Vec4 color) {
        this.color = color;
        return this;
    }

    private UIText font(UIFont font) {
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
