package strobeyworks.ui.primitives;

import static strobeyworks.ui.core.UIColors.col;

import strobeyworks.platform.ShaderManager;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.style.PrimitiveStyles;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.utils.Vec4;

public class UICircle extends UIElement {
    
    private static final int PRIM_TYPE_OVAL = 2;
    private static final int PRIM_TYPE_CIRCLE = 3;
    
    private Vec4 color;
    private boolean borderEnabled;
    private Vec4 borderColor;
    private float borderThickness;
    private boolean oval;
    
    public UICircle(UILength width, UILength height) {
        super(width, height);
        color = col(UIColors.TRANSPARENT);
        borderEnabled = false;
        borderColor = col(UIColors.WHITE);
        borderThickness = 2f;
        oval = true;
    }
    
    public UICircle() {
        super();
        color = col(UIColors.TRANSPARENT);
        borderEnabled = false;
        borderColor = col(UIColors.WHITE);
        borderThickness = 2f;
        oval = true;
    }
    
    @Override
    public void setRenderUniforms(ShaderManager sM) {
        sM.setUniformInt("uPrimType", oval ? PRIM_TYPE_OVAL : PRIM_TYPE_CIRCLE);
        sM.setUniformVec4("uColor", color);
        
        sM.setUniformInt("uHasBorder", borderEnabled ? 1 : 0);
        sM.setUniformVec4("uBorderColor", borderColor);
        sM.setUniformFloat("uBorderThickness", borderThickness);
        
        super.setRenderUniforms(sM);
    }

    @Override
    public void applyStyle(UIStyle style) {
        super.applyStyle(style);

        style.ifPresent(PrimitiveStyles.COLOR, this::color);
        style.ifPresent(PrimitiveStyles.BORDER_ENABLED, this::borderEnabled);
        style.ifPresent(PrimitiveStyles.BORDER_COLOR, this::borderColor);
        style.ifPresent(PrimitiveStyles.BORDER_THICKNESS, this::borderThickness);
    }

    @Override
    public UIStyle captureStyle() {
        UIStyle style = super.captureStyle();

        style.set(PrimitiveStyles.COLOR, color);
        style.set(PrimitiveStyles.BORDER_ENABLED, borderEnabled);
        style.set(PrimitiveStyles.BORDER_COLOR, borderColor);
        style.set(PrimitiveStyles.BORDER_THICKNESS, borderThickness);
        return style;
    }
    
    public UICircle color(Vec4 color) {
        this.color = color;
        return this;
    }
    
    public UICircle borderEnabled(boolean borderEnabled) {
        this.borderEnabled = borderEnabled;
        return this;
    }
    
    public UICircle borderColor(Vec4 borderColor) {
        this.borderColor = borderColor;
        return this;
    }
    
    public UICircle borderThickness(float borderThickness) {
        this.borderThickness = borderThickness;
        return this;
    }
    
    public UICircle oval(boolean oval) {
        this.oval = oval;
        return this;
    }
}
