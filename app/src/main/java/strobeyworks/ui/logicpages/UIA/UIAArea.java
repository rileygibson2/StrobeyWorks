package strobeyworks.ui.logicpages.UIA;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_L;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import strobeyworks.logger.Logger;
import strobeyworks.pipeline.RenderNode;
import strobeyworks.pipeline.RenderPipeline;
import strobeyworks.pipeline.RenderPipelineListener;
import strobeyworks.platform.IOEvent;
import strobeyworks.ui.components.input.notifications.UIBanner;
import strobeyworks.ui.components.input.notifications.UIBanner.UIBannerMode;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIRenderer;
import strobeyworks.ui.primitives.UIConnection;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.utils.Vec2;

public class UIAArea extends UIRectangle implements RenderPipelineListener {
    
    private int uiaCameraX;
    private int uiaCameraY;
    
    private float dragStartMouseX;
    private float dragStartMouseY;
    private int dragStartCameraX;
    private int dragStartCameraY;
    
    private List<UIANode> uiaNodes;
    private UIANode selectedNode;
    
    public UIAArea() {
        uiaCameraX = -100;
        uiaCameraY = -100;
        
        wantsPointer(true);
        clickable(true);
        focussable(true);
        
        style("color", UIColor.rgb(0.1f));
        style("overflow-x", UIOverflowMode.HIDDEN);
        style("overflow-y", UIOverflowMode.HIDDEN);
        
        RenderPipeline.getInstance().subscribe(this);
    }
    
    @Override
    public void outputtingNodeChanged(RenderNode node) {
        syncNodeVisuals();
    }
    
    @Override
    public void nodeInputsChanged(RenderNode node) {
        rebuildFromPipeline();
    }
    
    @Override
    public void pipelineFullyReloaded() {
        rebuildFromPipeline();
    }
    
    @Override
    public void pipelineSavedToFile(String fileName) {
        UIRenderer.getInstance().createBanner(
            UIBannerMode.SUCCESS,
            "Saved",
            "File: \""+fileName+"\""
        );
    }
    
    @Override
    public void pipelineLoadedFromFile(String fileName) {
        UIBanner banner = new UIBanner(
            "refresh_circle",
            UIColor.rgb(0.9f),
            "Loaded from file",
            "File: \""+fileName+"\""
        );
        
        UIRenderer.getInstance().addBanner(banner);
    }
    
    public void rebuildFromPipeline() {
        removeAllContentChildren();
        
        uiaNodes = new ArrayList<>();
        Set<RenderNode> nodes = RenderPipeline.getInstance().getAllNodes();
        
        // Add nodes
        for (RenderNode node : nodes) {
            UIANode n = new UIANode(this, node);
            uiaNodes.add(n);
            addChild(n);
        }
        
        // Add connections
        for (RenderNode node : nodes) {
            UIANode n1 = getUIForNode(node);
            List<RenderNode> connections = node.getDependancies();
            
            for (RenderNode connectingNode : connections) {
                UIANode n2 = getUIForNode(connectingNode);
                if (n2==null) continue;
                
                UIConnection c = new UIConnection(n1, n2);
                addChildAtIndex(0, c); 
            }
        }
        
        syncNodeVisuals();
        repositionElements();
    }
    
    private UIANode getUIForNode(RenderNode node) {
        for (UIANode uiaNode : uiaNodes) {
            if (uiaNode.getRenderNode()==node) return uiaNode;
        }
        return null;
    }
    
    public void syncNodeVisuals() {
        RenderNode activeNode = RenderPipeline.getInstance().getOutputtingNode();
        
        for (UIANode n : uiaNodes) {
            n.setOutputActiveVisuals(n.getRenderNode()==activeNode);
        }
    }
    
    private void repositionElements() {
        for (UIANode n : uiaNodes) {
            n.reposition();
        }
    }
    
    public Vec2 applyCamera(Vec2 pos) {
        return new Vec2(pos.x-uiaCameraX, pos.y-uiaCameraY);
    }
    
    public void selectNode(RenderNode node) {
        if (selectedNode!=null) {
            selectedNode.style("border-color", UIColor.rgb(0.3f))
            .style("z-index", 0);
        }
        UIRenderer.getInstance().getInspectorPane().selectRenderNode(node);
        
        if (node==null) return;
        for (UIANode uiaNode : uiaNodes) {
            if (uiaNode.getRenderNode()==node) {
                uiaNode.style("border-color", UIColor.rgb(0.3f, 0.5f, 0.3f))
                .style("z-index", 1);
                selectedNode = uiaNode;
            }
        }
    }
    
    @Override
    public void clicked(IOEvent event) {
        super.clicked(event);
        selectNode(null);
    }
    
    @Override
    public void gotPointer(IOEvent event) {
        super.gotPointer(event);
        dragStartMouseX = event.getMouseX();
        dragStartMouseY = event.getMouseY();
        
        dragStartCameraX = uiaCameraX;
        dragStartCameraY = uiaCameraY;
    }
    
    @Override
    public void handleIOEvent(IOEvent event) {
        switch (event.getEventType()) {
            case DRAG:
            float dx = event.getMouseX() - dragStartMouseX;
            float dy = event.getMouseY() - dragStartMouseY;
            
            uiaCameraX = dragStartCameraX - (int) dx;
            uiaCameraY = dragStartCameraY - (int) dy;
            
            repositionElements();
            break;
            
            case KEY_DOWN :
            handleKeyDown(event.getKeyCode());
            break;
            
            default:
            break;
        }
    }
    
    private void handleKeyDown(int keyCode) {
        //if (keyCode == GLFW_KEY_S) RenderPipeline.getInstance().savePipelineToDisk();
        //if (keyCode == GLFW_KEY_L) RenderPipeline.getInstance().loadPipelineFromDisk();
    }
}
