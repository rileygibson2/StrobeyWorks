package strobeyworks.ui.primitives;

import strobeyworks.ShaderManager;
import strobeyworks.utils.Vec3;
import strobeyworks.utils.Vec4;

public class UIRectangle extends UIElement {
    
    private Vec3 color;
    private Vec4 cornerRadius;
    private Vec3 borderColor;
    private float borderThickness;
    
    public UIRectangle(UIPair width, UIPair height) {
        super(width, height);
        color = new Vec3(0.1f);
        cornerRadius = new Vec4(0f);
        borderThickness = 2f;
    }
    
    public void setRenderUniforms(ShaderManager sM) {
        sM.setUniformVec3("uColor", color);
        sM.setUniformVec4("uCornerRadius", cornerRadius);

        sM.setUniformInt("uHasBorder", borderColor==null ? 0 : 1);
        if (borderColor!=null) {
            sM.setUniformVec3("uBorderColor", borderColor);
            sM.setUniformFloat("uBorderThickness", borderThickness);
        }
    }
    
    public void setColor(Vec3 baseColor) {this.color = baseColor;}
    
    public void setCornerRadius(Vec4 cornerRadii) {
        this.cornerRadius = cornerRadii;
    }
    
    public void setBorderColor(Vec3 borderColor) {this.borderColor = borderColor;}
    
    public void setBorderThickness(float borderThickness) {this.borderThickness = borderThickness;}
    
    public Vec3 getColor() {return this.color;}
    
    public Vec4 getCornerRadius() {return this.cornerRadius;}
}
