package strobeyworks.ui.primitives;

import static strobeyworks.ui.core.UILength.px;

import strobeyworks.pipeline.RenderNode;
import strobeyworks.platform.ShaderManager;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIRenderer;
import strobeyworks.utils.Vec2;

public class UIConnection extends UIRectangle {
    
    private UIElement from;
    private UIElement to;
    private float thickness;
    
    private Vec2 calcStart;
    private Vec2 calcEnd;
    
    public UIConnection(UIElement to, UIElement from) {
        this.to = to;
        this.from = from;
        thickness = 1f;
        
        style("position", UIPositionMode.ABSOLUTE);
        style("color", UIColor.rgb(0.7f));
        style("z-index", -1);
        reposition();
    }
    
    @Override
    public void layoutUpdated() {
        reposition();
    }
    
    private void reposition() {
        Vec2 a = new Vec2(
            from.getScreenX()+from.getScreenWidth(),
            from.getScreenY()+from.getScreenHeight() * 0.5f
        );
        
        Vec2 b = new Vec2(
            to.getScreenX(),
            to.getScreenY()+to.getScreenHeight() * 0.5f
        );
        
        float minX = Math.min(a.x, b.x) - thickness;
        float minY = Math.min(a.y, b.y) - thickness;
        float maxX = Math.max(a.x, b.x) + thickness;
        float maxY = Math.max(a.y, b.y) + thickness;
        
        style("offset-left", px(minX));
        style("offset-top", px(minY));
        style("width", px(maxX - minX));
        style("height", px(maxY - minY));
        
        calcStart = new Vec2(a.x - minX, a.y - minY);
        calcEnd = new Vec2(b.x - minX, b.y - minY);
    }
    
    @Override
    public void setRenderUniforms(ShaderManager sM) {
        super.setRenderUniforms(sM);
        
        sM.setUniformVec2("uStart", calcStart);
        sM.setUniformVec2("uEnd", calcEnd);
        sM.setUniformFloat("uThickness", thickness);
    }
    
    @Override
    public void render(UIRenderer renderer, ShaderManager sM) {
        renderer.renderConnection(sM, this);
    }
}
