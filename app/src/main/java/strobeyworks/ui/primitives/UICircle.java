package strobeyworks.ui.primitives;

import strobeyworks.ShaderManager;
import strobeyworks.ui.UIColors;
import strobeyworks.utils.Vec3;

public class UICircle extends UIElement {
    
    private static final int PRIM_TYPE_OVAL = 2;
    private static final int PRIM_TYPE_CIRCLE = 3;
    
    private Vec3 color;
    private Vec3 borderColor;
    private float borderThickness;
    private boolean oval;

    private Vec3 debugColor = UIColors.color(UIColors.RED);
    private boolean debugEnabled;
    
    public UICircle(UIPair width, UIPair height) {
        super(width, height);
        color = new Vec3(0.1f);
        borderThickness = 2f;
        oval = true;

        debugEnabled = false;
    }
    
    public UICircle() {
        super();
        color = new Vec3(0.1f);
        borderThickness = 2f;
        oval = true;

        debugEnabled = false;
    }
    
    public void setRenderUniforms(ShaderManager sM) {
        sM.setUniformInt("uPrimType", oval ? PRIM_TYPE_OVAL : PRIM_TYPE_CIRCLE);
        sM.setUniformVec3("uColor", color);
        
        sM.setUniformInt("uHasBorder", borderColor==null ? 0 : 1);
        if (borderColor!=null) {
            sM.setUniformVec3("uBorderColor", borderColor);
            sM.setUniformFloat("uBorderThickness", borderThickness);
        }

        sM.setUniformVec3("uDebugColor", debugColor);
        sM.setUniformInt("uDebugEnabled", debugEnabled ? 1 : 0);
    }
    
    public UICircle color(Vec3 baseColor) {
        this.color = baseColor;
        return this;
    }
    
    public UICircle borderColor(Vec3 borderColor) {
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

    public UICircle enableDebugColor(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
        return this;
    }
    
    public Vec3 getColor() {return this.color;}
}
