package strobeyworks.ui.primitives;

import strobeyworks.ShaderManager;
import strobeyworks.utils.Vec3;
import strobeyworks.utils.Vec4;

public class UIRectangle extends UIElement {

    private Vec3 color;
    private Vec4 cornerRadius;
    
    public UIRectangle(UIPair x, UIPair y, UIPair width, UIPair height) {
        super(x, y, width, height);
        color = new Vec3(0.1f);
        cornerRadius = new Vec4(0f);
    }

    public void setRenderUniforms(ShaderManager sM) {
        sM.setUniformVec3("uColor", color);
        sM.setUniformVec4("uCornerRadius", cornerRadius);
    }
    
    public void setColor(Vec3 baseColor) {this.color = baseColor;}

    public void setCornerRadius(Vec4 cornerRadii) {
        this.cornerRadius = cornerRadii;
    }

    public Vec3 getColor() {return this.color;}

    public Vec4 getCornerRadius() {return this.cornerRadius;}
}
