package strobeyworks.ui.logicpages.UIA;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.nodes.RenderNode;
import strobeyworks.platform.IOEvent;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFontManager;
import strobeyworks.ui.primitives.UICircle;
import strobeyworks.ui.primitives.UIRectFactory;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.ui.style.StyleProps;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.utils.Vec2;
import strobeyworks.utils.Vec4;

public class UIANode extends UIRectangle {
    
    private UIAArea area;
    private RenderNode renderNode;
    
    private float dragStartMouseX;
    private float dragStartMouseY;
    private int dragStartPosX;
    private int dragStartPosY;

    private UICircle ballL;
    private UICircle ballR;
    
    public UIANode(UIAArea area, RenderNode node) {
        this.area = area;
        this.renderNode = node;
        
        wantsPointer(true);
        clickable(true);
        hitOverflowVisibleChildren(true);
        
        style("width", px(100));
        style("height", px(80));
        style("position", UIPositionMode.ABSOLUTE);
        
        style("color", UIColor.rgb(0.3f));
        style("corner-radius", new Vec4(2));
        style("flow-direction", UIFlowDirection.COLUMN);
        
        style("border-enabled", false);
        style("border-thickness", px(2));
        style("border-color", UIColor.rgb(0.3f, 0.5f, 0.3f));
        
        // Title
        UIRectangle title = UIRectFactory.sizedAligned(pcw(1f), pch(0.3f));
        title.style("position", UIPositionMode.ABSOLUTE)
        .style("offset-top", pch(0.7f))
        .style("padding-left", px(3))
        .style("color", UIColor.rgb(0.2f));
        
        UIText text = new UIText(UIFontManager.getUIFont("RobotoMono-Medium.ttf", 14f), node.getCustomName());
        text.style("color", UIColor.rgb(0.7f));
        
        // Content
        UIRectangle cArea = UIRectFactory.sizedAligned(pcw(1f), pch(0.7f));
        cArea.style("position", UIPositionMode.ABSOLUTE)
        .style("color", UIColor.rgb(0.3f));
        
        // Balls
        UIStyle hover = new UIStyle();
        hover.set(StyleProps.TRANSFORM_SCALEX, 2f);
        hover.set(StyleProps.TRANSFORM_SCALEY, 2f);

        ballR = new UICircle();
        ballR.style("width", pcw(0.1f))
        .style("height", pch(0.1f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-top", pch(0.4f))
        .style("offset-left", pcw(0.95f))
        .style("oval", false)
        .style("color", UIColor.rgb(0.3f))
        .style("transition-duration", 0.15f)
        .style(StyleProps.TRANSFORM_SCALEX, 1.0f)
        .style(StyleProps.TRANSFORM_SCALEY, 1.0f)
        .hoverable(true)
        .hoverStyle(hover);

        ballL = new UICircle();
        ballL.style("width", pcw(0.1f))
        .style("height", pch(0.1f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-top", pch(0.4f))
        .style("offset-left", pcw(-0.05f))
        .style("oval", false)
        .style("color", UIColor.rgb(0.3f))
        .style("transition-duration", 0.15f)
        .style(StyleProps.TRANSFORM_SCALEX, 1.0f)
        .style(StyleProps.TRANSFORM_SCALEY, 1.0f)
        .hoverable(true)
        .hoverStyle(hover);
        
        
        title.addChild(text);
        
        addChild(cArea);
        addChild(ballL);
        addChild(ballR);
        addChild(title);
    }
    
    public void reposition() {
        Vec2 pos = area.applyCamera(renderNode.getUIAPosition());
        style("offset-left", px(pos.x));
        style("offset-top", px(pos.y));
    }
    
    @Override
    public void clicked(IOEvent event) {
        super.clicked(event);
        
        area.selectNode(renderNode);
    }
    
    @Override
    public void gotPointer(IOEvent event) {
        super.gotPointer(null);
        dragStartMouseX = event.getMouseX();
        dragStartMouseY = event.getMouseY();
        
        dragStartPosX = (int) renderNode.getUIAPosition().x;
        dragStartPosY = (int) renderNode.getUIAPosition().y;
    }
    
    @Override
    public void handleIOEvent(IOEvent event) {
        switch (event.getEventType()) {
            case DRAG:
            float dx = event.getMouseX() - dragStartMouseX;
            float dy = event.getMouseY() - dragStartMouseY;
            
            int posX = dragStartPosX - (int) -dx;
            int posY = dragStartPosY - (int) -dy;
            renderNode.setUIAPosition(new Vec2(posX, posY));
            reposition();
            break;
            
            default:
            break;
        }
    }
    
    public RenderNode getRenderNode() {
        return this.renderNode;
    }

    public UICircle getLeftBall() {
        return ballL;
    }

    public UICircle getRightBall() {
        return ballR;
    }
}
