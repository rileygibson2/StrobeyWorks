package strobeyworks.ui.components.input;

import static strobeyworks.ui.core.UILength.pph;
import static strobeyworks.ui.core.UILength.ppw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.platform.IOEvent;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.primitives.UICircle;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.style.StyleProps;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.utils.Utils;
import strobeyworks.utils.Vec4;

public class UISlider extends UIBindableInput<Float, Float> {    
    
    private UIRectangle knob;
    private UIRectangle followingRect;
    private float bounds = 0.99f;
    
    public UISlider(UIValueMapper<Float, Float> adaptor) {
        super(adaptor);
        
        wantsPointer(true);
        scrollable(true);
        
        style("box", UIBoxMode.FIXED);
        style("flow-direction", UIFlowDirection.ROW);
        style("flow-wrap", false);
        style("padding-left", px(0));
        style("padding-right", px(0));
        style("padding-top", px(0));
        style("padding-bottom", px(0));
        style("align-items", UIAlignItems.CENTER);
        style("border-enabled", false);
        style("color", UIColor.rgb(0.14f));
        
        knob = new UIRectangle();
        knob.style("width", px(2))
        .style("height", pph(1f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-left", pph(0.05f))
        .style("border-color", UIColor.green())
        .style("color", UIColor.rgb(0.6f))
        .style("border-enabled", false)
        .style("transition-duration", 0.2f)
        .style(StyleProps.TRANSFORM_SCALEX, 1.0f)
        .style(StyleProps.TRANSFORM_SCALEY, 1.0f)
        .hoverable(true);
        
        followingRect = new UIRectangle();
        followingRect.style("width", ppw(0f))
        .style("height", pph(1f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("color", UIColor.rgb(0.4f))
        .style("corner-radius", new Vec4(2f));
        
        addChild(followingRect);
        addChild(knob);
    }
    
    @Override
    protected Float getDefaultLocalValue() {
        return 0f;
    }
    
    @Override
    protected void implementLocalValueOnUI() {
        float value = getLocalValue();
        float offset = (1-bounds)*0.5f;
        float parentW = getLocalWidth();
        float knobW = knob.getLocalWidth();
        float fullTravel = parentW-knobW;
        
        float cV = offset * fullTravel + value * (fullTravel * bounds);
        knob.style("offset-left", px(cV));
        
        float rV = cV + knobW * 0.5f;
        followingRect.style("width", px(rV));
        
        float a = Utils.smoothFalloffBefore(0.05f, value);
        followingRect.style("opacity", a);
    }
    
    private void setValueFromMouse(float mouseX) {
        float localX = mouseX - getScreenX();
        float value = localX / getScreenWidth();
        
        setLocalValue(Math.max(Math.min(value, 1f), 0f));
        commitLocalValue();
    }
    
    @Override
    public void gotPointer(IOEvent event) {
        setValueFromMouse(event.getMouseX());
        super.gotPointer(event);
    }
    
    @Override
    public void lostPointer(IOEvent event) {
        setValueFromMouse(event.getMouseX());
        super.lostPointer(event);
    }
    
    @Override
    public void handleIOEvent(IOEvent event) {
        switch (event.getEventType()) {
            case DRAG :
            setValueFromMouse(event.getMouseX());
            break;
            
            case SCROLL:
            float delta = -event.getScrollY() * 0.02f;
            setLocalValue(Math.max(0f, Math.min(getLocalValue() + delta, 1f)));
            commitLocalValue();
            break;
            
            default: return;
        }
    }
}
