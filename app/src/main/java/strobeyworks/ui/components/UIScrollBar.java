package strobeyworks.ui.components;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.pph;
import static strobeyworks.ui.core.UILength.ppw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.platform.IOEvent;
import strobeyworks.platform.Transition;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIRenderer;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.style.StyleProps;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.utils.Utils;
import strobeyworks.utils.Vec4;

public class UIScrollBar extends UIRectangle {
    
    public enum ScrollAxis {
        HORIZONTAL,
        VERTICAL
    }
    
    private ScrollAxis axis;
    private float grabOffset;
    private boolean stayVisible;
    
    private Transition fadeOut;
    
    public UIScrollBar(ScrollAxis axis) {
        this.axis = axis;
        
        hoverable(true);
        wantsPointer(true);
        
        style("position", UIPositionMode.ABSOLUTE_FIXED);
        style("corner-radius", new Vec4(10f));
        style("color", col(UIColors.GREEN));
        style("transition-duration", 0.2f);
        style("opacity", 0.5f);
        
        if (axis==ScrollAxis.HORIZONTAL) {
            style("height", px(10));
            style("width", ppw(1f));
            style("offset-top", pph(0.9f));
        }
        
        if (axis==ScrollAxis.VERTICAL) {
            style("height", pph(1f));
            style("width", px(10));
            style("offset-left", ppw(0.9f));
        }
        
        UIStyle s = new UIStyle();
        s.set(StyleProps.OPACITY, 1f);
        hoverStyle(s);
    }

    @Override
    public void initialise() {
        getParent().updateScrollBars();
        super.initialise();
    }
    
    public void setThumb(float totalSize, float viewportSize, float scroll) {
        if (axis==ScrollAxis.HORIZONTAL) {
            // Set position from scroll
            float w = totalSize <= 0f ? 1f : Math.min(1f, viewportSize / totalSize);
            style("width", ppw(w));
            
            float travel = Math.max(0f, 1-w);
            float l = travel*scroll;
            style("offset-left", ppw(l));
            
            // Set position in parent
            float oT = getParent().getScreenPaddingBoxBounds().getHeight()-getScreenHeight()-2;
            style("offset-top", px(oT));
            
        }
        
        if (axis==ScrollAxis.VERTICAL) {
            // Set position from scroll
            float h = totalSize <= 0f ? 1f : Math.min(1f, viewportSize / totalSize);
            style("height", pph(h));
            
            float travel = Math.max(0f, 1-h);
            float t = travel*scroll;
            style("offset-top", pph(t));
            
            // Set position in parent
            float oT = getParent().getLocalPaddingBoxWidth()-getScreenWidth()-2;
            style("offset-left", px(oT));
        }
    }
    
    private void setValueFromMouse(IOEvent event) {
        if (axis==ScrollAxis.HORIZONTAL) {
            float leftEdge = event.getMouseX()-getParent().getScreenX()-grabOffset;
            float travel = getParent().getScreenWidth()-getScreenWidth();
            if (travel<=0f) return;
            
            float scroll = leftEdge/travel;
            getParent().scrollX(Utils.clamp01(scroll));
        }
        
        if (axis==ScrollAxis.VERTICAL) {
            float topEdge = event.getMouseY()-getParent().getScreenY()-grabOffset;
            float travel = getParent().getScreenHeight()-getScreenHeight();
            if (travel<=0f) return;
            
            float scroll = topEdge/travel;
            getParent().scrollY(Utils.clamp01(scroll));
        }
    }
    
    @Override
    public void gotHover(IOEvent event) {
        if (fadeOut!=null) fadeOut.interrupt();
        super.gotHover(event);
    }
    
    @Override
    public void lostHover(IOEvent event) {
        fadeOut = new Transition(1f, 2f, null);
        UIStyle s = new UIStyle();
        s.set(StyleProps.OPACITY, 0f);

        transitionToStyle(s, fadeOut);
        super.lostHover(event);
    }
    
    @Override
    public void gotPointer(IOEvent event) {
        if (axis==ScrollAxis.HORIZONTAL) grabOffset = event.getMouseX()-getScreenX();
        if (axis==ScrollAxis.VERTICAL) grabOffset = event.getMouseY()-getScreenY();
    }
    
    @Override
    public void lostPointer(IOEvent event) {}
    
    @Override
    public void handleIOEvent(IOEvent event) {
        switch (event.getEventType()) {
            case DRAG :
            setValueFromMouse(event);
            break;
            
            default: return;
        }
    }
}
