package strobeyworks.ui.primitives;

import strobeyworks.platform.ShaderManager;
import strobeyworks.ui.UIColors;
import strobeyworks.utils.Vec3;
import strobeyworks.utils.Vec4;

public class UICircle extends UIElement {
    
    private static final int PRIM_TYPE_OVAL = 2;
    private static final int PRIM_TYPE_CIRCLE = 3;
    
    private Vec4 color;
    private boolean borderEnabled;
    private Vec4 borderColor;
    private float borderThickness;
    private boolean oval;
    
    private Vec4 debugColor = UIColors.color(UIColors.RED);
    private boolean debugEnabled;
    
    public UICircle(UIPair width, UIPair height) {
        super(width, height);
        color = UIColors.color(UIColors.TRANSPARENT);
        borderEnabled = false;
        borderColor = UIColors.color(UIColors.WHITE);
        borderThickness = 2f;
        oval = true;
        
        debugEnabled = false;
    }
    
    public UICircle() {
        super();
        color = UIColors.color(UIColors.TRANSPARENT);
        borderEnabled = false;
        borderColor = UIColors.color(UIColors.WHITE);
        borderThickness = 2f;
        oval = true;
        
        debugEnabled = false;
    }
    
    public void setRenderUniforms(ShaderManager sM) {
        sM.setUniformInt("uPrimType", oval ? PRIM_TYPE_OVAL : PRIM_TYPE_CIRCLE);
        sM.setUniformVec4("uColor", color);
        
        sM.setUniformInt("uHasBorder", borderEnabled ? 1 : 0);
        sM.setUniformVec4("uBorderColor", borderColor);
        sM.setUniformFloat("uBorderThickness", borderThickness);
        
        sM.setUniformVec4("uDebugColor", debugColor);
        sM.setUniformInt("uDebugEnabled", debugEnabled ? 1 : 0);
    }
    
    public UICircle color(Vec4 color) {
        this.color = color;
        return this;
    }
    
    public UICircle enableBorder(boolean borderEnabled) {
        this.borderEnabled = borderEnabled;
        return this;
    }
    
    public UICircle borderColor(Vec4 borderColor) {
        this.borderColor = borderColor;
        enableBorder(true);
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
    
    public UICircle enableDebugColor(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
        return this;
    }
}
