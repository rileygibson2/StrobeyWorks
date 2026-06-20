package strobeyworks.ui.primitives;

import static strobeyworks.ui.core.UILength.px;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.pch;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import strobeyworks.platform.ShaderManager;
import strobeyworks.ui.core.UIColor;
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
    
    private UIColor color;
    private Vec4 cornerRadius;
    private UIColor borderColor;
    
    public UIRectangle() {
        super();
        style("color", UIColor.transparent());
        style("corner-radius", new Vec4(0f));
        style("border-color", UIColor.white());
        style("border-thickness", px(2));
    }
    
    public void setRenderUniforms(ShaderManager sM) {
        sM.setUniformInt("uPrimType", PRIM_TYPE);
        sM.setUniformVec4("uColor", new Vec4(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
        sM.setUniformVec4("uCornerRadius", cornerRadius);
        sM.setUniformVec4("uBorderColor", new Vec4(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), borderColor.getAlpha()));
        
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
    
    private UIRectangle color(UIColor color) {
        this.color = color;
        return this;
    }
    
    private UIRectangle cornerRadius(Vec4 radii) {
        this.cornerRadius = radii;
        return this;
    }
    
    private UIRectangle borderColor(UIColor borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public static UIRectangle fullContentCollumn() {
        UIRectangle r = new UIRectangle();
        r.style("width", pcw(1.0f))
        .style("height", pch(1.0f))
        .style("flow-direction", UIFlowDirection.COLUMN);
        return r;
    }

    public static UIRectangle fullContentRow() {
        UIRectangle r = new UIRectangle();
        r.style("width", pcw(1.0f))
        .style("height", pch(1.0f))
        .style("flow-direction", UIFlowDirection.ROW);
        return r;
    }

    public static UIRectangle defaultRect(UILength width, UILength height) {
        UIRectangle r = new UIRectangle();
        r.style("width", width)
        .style("height", height);
        return r;
    }
}
