package strobeyworks.ui.primitives;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.px;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import strobeyworks.platform.ShaderManager;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.style.StyleProps;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.ui.style.UIStyleProperty;
import strobeyworks.utils.Vec4;

public class UIRectangle extends UIElement {
    
    private static final Map<UIStyleProperty<?>, BiConsumer<UIRectangle, Object>> APPLIERS = new HashMap<>();
    
    static {
        register(APPLIERS, StyleProps.COLOR, UIRectangle::color);
        register(APPLIERS, StyleProps.CORNER_RADIUS, UIRectangle::cornerRadius);
        register(APPLIERS, StyleProps.BORDER_COLOR, UIRectangle::borderColor);
    }
    
    private static final int PRIM_TYPE = 1;
    
    private Vec4 color;
    private Vec4 cornerRadius;
    private Vec4 borderColor;
    
    public UIRectangle() {
        super();
        style("color", col(UIColors.TRANSPARENT));
        style("corner-radius", new Vec4(0f));
        style("border-color", col(UIColors.WHITE));
        style("border-thickness", px(2));
    }
    
    public void setRenderUniforms(ShaderManager sM) {
        sM.setUniformInt("uPrimType", PRIM_TYPE);
        sM.setUniformVec4("uColor", color);
        sM.setUniformVec4("uCornerRadius", cornerRadius);
        sM.setUniformVec4("uBorderColor", borderColor);
        
        super.setRenderUniforms(sM);
    }
    
    @Override
    protected void applyStyleProperty(UIStyleProperty<?> property, Object value) {
        BiConsumer<UIRectangle, Object> applier = APPLIERS.get(property);
        if (applier!=null) applier.accept(this, value);
        else super.applyStyleProperty(property, value);
    }
    
    @Override
    protected UIStyle captureStyle() {
        UIStyle style = super.captureStyle();
        
        style.set(StyleProps.COLOR, color);
        style.set(StyleProps.CORNER_RADIUS, cornerRadius);
        return style;
    }
    
    private UIRectangle color(Vec4 color) {
        this.color = color;
        return this;
    }
    
    private UIRectangle cornerRadius(Vec4 radii) {
        this.cornerRadius = radii;
        return this;
    }
    
    private UIRectangle borderColor(Vec4 borderColor) {
        this.borderColor = borderColor;
        return this;
    }
}
