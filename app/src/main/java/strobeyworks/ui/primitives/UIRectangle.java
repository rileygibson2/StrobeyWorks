package strobeyworks.ui.primitives;

import static strobeyworks.ui.core.UIColors.col;

import java.util.HashSet;
import java.util.Set;

import strobeyworks.platform.ShaderManager;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.style.PrimitiveStyles;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.ui.style.UIStyleProperty;
import strobeyworks.utils.Vec4;

public class UIRectangle extends UIElement {
    
    private static final int PRIM_TYPE = 1;
    
    private Vec4 color;
    private Vec4 cornerRadius;
    private Boolean borderEnabled;
    private Vec4 borderColor;
    private float borderThickness;
    private boolean borderLeft;
    private boolean borderRight;
    private boolean borderTop;
    private boolean borderBottom;
    
    public UIRectangle(UILength width, UILength height) {
        super(width, height);
        color = col(UIColors.TRANSPARENT);
        cornerRadius = new Vec4(0f);
        borderEnabled = false;
        borderColor = col(UIColors.TRANSPARENT);
        borderThickness = 2f;
        borderLeft = true;
        borderRight = true;
        borderTop = true;
        borderBottom = true;
    }
    
    public UIRectangle() {
        super();
        color = col(UIColors.TRANSPARENT);
        cornerRadius = new Vec4(0f);
        borderEnabled = false;
        borderColor = col(UIColors.TRANSPARENT);
        borderThickness = 2f;
        borderLeft = true;
        borderRight = true;
        borderTop = true;
        borderBottom = true;
    }
    
    public void setRenderUniforms(ShaderManager sM) {
        sM.setUniformInt("uPrimType", PRIM_TYPE);
        sM.setUniformVec4("uColor", color);
        sM.setUniformVec4("uCornerRadius", cornerRadius);
        sM.setUniformInt("uHasBorder", borderEnabled ? 1 : 0);
        sM.setUniformVec4("uBorderColor", borderColor);
        sM.setUniformFloat("uBorderThickness", borderThickness);
        sM.setUniformVec4("uBorderSides", new Vec4(
            borderTop ? 1f : 0f,
            borderRight ? 1f : 0f,
            borderBottom ? 1f : 0f,
            borderLeft ? 1f : 0f
        ));
        
        super.setRenderUniforms(sM);
    }

    @Override
    public void applyStyle(UIStyle style) {
        super.applyStyle(style);

        style.ifPresent(PrimitiveStyles.COLOR, this::color);
        style.ifPresent(PrimitiveStyles.CORNER_RADIUS, this::cornerRadius);
        style.ifPresent(PrimitiveStyles.BORDER_ENABLED, this::borderEnabled);
        style.ifPresent(PrimitiveStyles.BORDER_COLOR, this::borderColor);
        style.ifPresent(PrimitiveStyles.BORDER_THICKNESS, this::borderThickness);
        style.ifPresent(PrimitiveStyles.BORDER_LEFT, this::borderLeft);
        style.ifPresent(PrimitiveStyles.BORDER_RIGHT, this::borderRight);
        style.ifPresent(PrimitiveStyles.BORDER_TOP, this::borderTop);
        style.ifPresent(PrimitiveStyles.BORDER_BOTTOM, this::borderBottom);
    }

    @Override
    public UIStyle captureStyle() {
        UIStyle style = super.captureStyle();
        
        style.set(PrimitiveStyles.COLOR, color);
        style.set(PrimitiveStyles.CORNER_RADIUS, cornerRadius);
        style.set(PrimitiveStyles.BORDER_ENABLED, borderEnabled);
        style.set(PrimitiveStyles.BORDER_COLOR, borderColor);
        style.set(PrimitiveStyles.BORDER_THICKNESS, borderThickness);
        style.set(PrimitiveStyles.BORDER_LEFT, borderLeft);
        style.set(PrimitiveStyles.BORDER_RIGHT, borderRight);
        style.set(PrimitiveStyles.BORDER_TOP, borderTop);
        style.set(PrimitiveStyles.BORDER_BOTTOM, borderBottom);
        return style;
    }

    @Override
    public Set<UIStyleProperty<?>> getValidStyleProperties() {
        Set<UIStyleProperty<?>> s = new HashSet<>(super.getValidStyleProperties());
        s.addAll(Set.of(
            PrimitiveStyles.COLOR,
            PrimitiveStyles.CORNER_RADIUS,
            PrimitiveStyles.BORDER_ENABLED,
            PrimitiveStyles.BORDER_COLOR,
            PrimitiveStyles.BORDER_THICKNESS,
            PrimitiveStyles.BORDER_LEFT,
            PrimitiveStyles.BORDER_RIGHT,
            PrimitiveStyles.BORDER_TOP,
            PrimitiveStyles.BORDER_BOTTOM
        ));
        return s;
    }
    
    public UIRectangle color(Vec4 color) {
        this.color = color;
        return this;
    }
    
    public UIRectangle cornerRadius(Vec4 cornerRadii) {
        this.cornerRadius = cornerRadii;
        return this;
    }

    public UIRectangle cornerRadius(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.cornerRadius = new Vec4(topLeft, topRight, bottomRight, bottomLeft);
        return this;
    }
    
    public UIRectangle cornerRadius(float cornerRadius) {
        this.cornerRadius = new Vec4(cornerRadius);
        return this;
    }
    
    public UIRectangle borderEnabled(boolean borderEnabled) {
        this.borderEnabled = borderEnabled;
        return this;
    }
    
    public UIRectangle borderColor(Vec4 borderColor) {
        this.borderColor = borderColor;
        return this;
    }
    
    public UIRectangle borderThickness(float borderThickness) {
        this.borderThickness = borderThickness;
        return this;
    }
    
    public UIRectangle borderLeft(boolean borderLeft) {
        this.borderLeft = borderLeft;
        return this;
    }
    
    public UIRectangle borderRight(boolean borderRight) {
        this.borderRight = borderRight;
        return this;
    }
    
    public UIRectangle borderTop(boolean borderTop) {
        this.borderTop = borderTop;
        return this;
    }
    
    public UIRectangle borderBottom(boolean borderBottom) {
        this.borderBottom = borderBottom;
        return this;
    }

    public Vec4 getColor() {return this.color;}
}
