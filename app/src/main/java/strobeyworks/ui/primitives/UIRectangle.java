package strobeyworks.ui.primitives;

import static strobeyworks.ui.UIColors.col;

import strobeyworks.platform.ShaderManager;
import strobeyworks.ui.UIColors;
import strobeyworks.utils.Vec4;

public class UIRectangle extends UIElement {
    
    private static final int PRIM_TYPE = 1;
    
    private Vec4 color;
    private Vec4 cornerRadius;
    
    private boolean borderEnabled;
    private Vec4 borderColor;
    private float borderThickness;
    private boolean borderLeft;
    private boolean borderRight;
    private boolean borderTop;
    private boolean borderBottom;
    
    private Vec4 debugColor = col(UIColors.RED);
    private boolean debugEnabled;
    
    public UIRectangle(UIPair width, UIPair height) {
        super(width, height);
        color = col(UIColors.TRANSPARENT);
        cornerRadius = new Vec4(0f);
        
        borderEnabled = false;
        borderColor = col(UIColors.WHITE);
        borderThickness = 2f;
        borderLeft = true;
        borderRight = true;
        borderTop = true;
        borderBottom = true;
        
        debugEnabled = false;
    }
    
    public UIRectangle() {
        super();
        color = col(UIColors.TRANSPARENT);
        cornerRadius = new Vec4(0f);
        
        borderEnabled = false;
        borderColor = col(UIColors.WHITE);
        borderThickness = 2f;
        borderLeft = true;
        borderRight = true;
        borderTop = true;
        borderBottom = true;
        
        debugEnabled = false;
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
        
        sM.setUniformVec4("uDebugColor", debugColor);
        sM.setUniformInt("uDebugEnabled", debugEnabled ? 1 : 0);
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
    
    public UIRectangle enableBorder(boolean borderEnabled) {
        this.borderEnabled = borderEnabled;
        return this;
    }
    
    public UIRectangle borderColor(Vec4 borderColor) {
        this.borderColor = borderColor;
        enableBorder(true);
        return this;
    }
    
    public UIRectangle borderThickness(float borderThickness) {
        this.borderThickness = borderThickness;
        enableBorder(true);
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
    
    public UIRectangle enableDebugColor(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
        return this;
    }

    public Vec4 getColor() {return this.color;}
}
