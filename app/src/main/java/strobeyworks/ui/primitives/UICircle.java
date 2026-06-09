package strobeyworks.ui.primitives;

import static strobeyworks.ui.core.UILength.px;

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

public class UICircle extends UIElement {
    
    private static final Map<UIStyleProperty<?>, BiConsumer<UICircle, Object>> APPLIERS = new HashMap<>();

    static {
        register(APPLIERS, StyleProps.COLOR, UICircle::color);
        register(APPLIERS, StyleProps.BORDER_COLOR, UICircle::borderColor);
        register(APPLIERS, StyleProps.OVAL, UICircle::oval);
    }

    private static final int PRIM_TYPE_OVAL = 2;
    private static final int PRIM_TYPE_CIRCLE = 3;
    
    private UIColor color;
    private UIColor borderColor;
    private boolean oval;
    
    public UICircle() {
        super();
        
        style("color", UIColor.transparent());
        style("border-color", UIColor.white());
        style("border-thickness", px(2));
        style("oval", true);
    }
    
    @Override
    public void setRenderUniforms(ShaderManager sM) {
        sM.setUniformInt("uPrimType", oval ? PRIM_TYPE_OVAL : PRIM_TYPE_CIRCLE);
        sM.setUniformVec4("uColor", new Vec4(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
        
        sM.setUniformVec4("uBorderColor", new Vec4(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), borderColor.getAlpha()));
        super.setRenderUniforms(sM);
    }

    @Override
    protected void applyStyleProperty(UIStyleProperty<?> property, Object value) {
        BiConsumer<UICircle, Object> applier = APPLIERS.get(property);
        if (applier!=null) applier.accept(this, value);
        else super.applyStyleProperty(property, value);
    }

    @Override
    public UIStyle captureStyle() {
        UIStyle style = super.captureStyle();

        style.set(StyleProps.COLOR, color);
        style.set(StyleProps.BORDER_COLOR, borderColor);
        return style;
    }
    
    private UICircle color(UIColor color) {
        this.color = color;
        return this;
    }
    
    private UICircle borderColor(UIColor borderColor) {
        this.borderColor = borderColor;
        return this;
    }
    
    private UICircle oval(boolean oval) {
        this.oval = oval;
        return this;
    }
}
