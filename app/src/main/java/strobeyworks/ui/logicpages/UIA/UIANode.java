package strobeyworks.ui.logicpages.UIA;

import static strobeyworks.ui.core.UILength.pbw;
import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.pipeline.RenderNode;
import strobeyworks.pipeline.RenderPipeline;
import strobeyworks.platform.IOEvent;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFontManager;
import strobeyworks.ui.primitives.UIRectFactory;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.ui.primitives.UITextureView;
import strobeyworks.utils.Vec2;
import strobeyworks.utils.Vec4;

public class UIANode extends UIRectangle {
    
    private UIAArea area;
    private RenderNode renderNode;
    
    private UIRectangle outputBox;
    
    private float dragStartMouseX;
    private float dragStartMouseY;
    private int dragStartPosX;
    private int dragStartPosY;
    
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
        
        style("border-enabled", true);
        style("border-thickness", px(2));
        style("border-color", UIColor.rgb(0.3f));
        
        // Title
        UIRectangle title = UIRectFactory.sized_Align(pcw(1f), pch(0.3f));
        title.style("position", UIPositionMode.ABSOLUTE)
        .style("offset-top", pch(0.7f))
        .style("padding-left", px(3))
        .style("padding-right", px(3))
        .style("color", UIColor.rgb(0.2f));
        
        // Outputting symbol
        outputBox = UIRectFactory.sized_Center(px(15), px(15));
        outputBox.style("color", UIColor.rgb(0.6f))
        .style("corner-radius", new Vec4(2))
        .clickable(true)
        .onClicked(e -> {
            RenderPipeline.getInstance().setOutputtingNode(node);
        });
        
        UIText text = new UIText(UIFontManager.getUIFont("RobotoMono-Medium.ttf", 14f), node.getCustomName());
        text.style("color", UIColor.rgb(0.7f));
        
        // Content
        UIRectangle cArea = UIRectFactory.sized_Align(pcw(1f), pch(0.7f));
        cArea.style("position", UIPositionMode.ABSOLUTE)
        .style("color", UIColor.rgb(0.3f));
        
        UITextureView preview = new UITextureView(
            () -> renderNode.getRenderTarget().getTexture()
        );
        
        preview.style("width", pcw(1f))
        .style("height", pch(1f))
        .style("position", UIPositionMode.ABSOLUTE);
        
        title.addChild(text);
        title.addChild(UIRectFactory.rowGrow(1f));
        title.addChild(outputBox);
        cArea.addChild(preview);
        
        addChild(cArea);
        addChild(title);
    }
    
    public void reposition() {
        Vec2 pos = area.applyCamera(renderNode.getUIAPosition());
        style("offset-left", px(pos.x));
        style("offset-top", px(pos.y));
    }
    
    public void setOutputActiveVisuals(boolean active) {
        outputBox.style("color", active ? UIColor.rgb(0.3f, 0.7f, 0.3f) : UIColor.rgb(0.4f));
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
}
