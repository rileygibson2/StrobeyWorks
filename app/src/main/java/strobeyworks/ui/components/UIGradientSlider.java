package strobeyworks.ui.components;

import static strobeyworks.ui.core.UILength.pph;
import static strobeyworks.ui.core.UILength.ppw;
import static strobeyworks.ui.core.UILength.px;

import java.util.ArrayList;
import java.util.List;

import strobeyworks.platform.IOEvent;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.primitives.UICircle;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.style.StyleProps;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.utils.Bindable;
import strobeyworks.utils.BindableObserver;
import strobeyworks.utils.Utils;
import strobeyworks.utils.Vec4;

public class UIGradientSlider extends UIRectangle implements BindableObserver<UIColor> {    
    
    private static class GradientStop {
        Bindable<UIColor> color;
        float position;
        UICircle knob;
        
        GradientStop(Bindable<UIColor> color, float position, UICircle knob) {
            this.color = color;
            this.position = position;
            this.knob = knob;
        }
    }
    
    private List<GradientStop> stops;
    private float bounds = 0.99f;

    private GradientStop draggingStop;

    private UIGenericCallback<Bindable<UIColor>> activeCallback;
    
    public UIGradientSlider() {
        stops = new ArrayList<>();
        
        wantsPointer(true);
        
        style("box", UIBoxMode.FIXED);
        style("flow-direction", UIFlowDirection.ROW);
        style("flow-wrap", false);
        style("padding-left", px(0));
        style("padding-right", px(0));
        style("padding-top", px(0));
        style("padding-bottom", px(0));
        style("align-items", UIAlignItems.CENTER);
        
        style("border-enabled", true);
        style("border-color", UIColor.green());
        style("color", UIColor.gray008());
        style("corner-radius", new Vec4(20f));
    }

    @Override
    public void initialise() {
        positionKnobs();
    }

    public void setActiveCallback(UIGenericCallback<Bindable<UIColor>> callback) {
        this.activeCallback = callback;
    }
    
    public void addStop(Bindable<UIColor> color, float position) {
        position = Utils.clamp01(position);

        UICircle knob = new UICircle();
        knob.style("width", pph(1f))
        .style("height", pph(1f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-left", pph(0.05f));
        
        UIStyle style = new UIStyle();
        style.set(StyleProps.TRANSFORM_SCALEX, 1.2f)
        .set(StyleProps.TRANSFORM_SCALEY, 1.2f);
        
        knob.style("border-color", UIColor.green())
        .style("color", UIColor.gray008())
        .style("oval", false)
        .style("border-enabled", true)
        .style("transition-duration", 0.2f)
        .style(StyleProps.TRANSFORM_SCALEX, 1.0f)
        .style(StyleProps.TRANSFORM_SCALEY, 1.0f)
        .hoverable(true)
        .hoverStyle(style);
        
        UICircle knobInner = new UICircle();
        knobInner.style("width", ppw(0.8f))
        .style("height", pph(0.8f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-top", ppw(0.1f))
        .style("offset-left", pph(0.1f))
        .style("border-color", UIColor.green())
        .style("color", UIColor.gray008())
        .style("oval", false)
        .style("border-enabled", true);
        
        color.bind(this);
        knob.style("color", color.getValue().clone());

        stops.add(new GradientStop(color, position, knob));
        
        knob.addChild(knobInner);
        addChild(knob);
        
        if (isInitialised()) positionKnobs();
    }
    
    public void removeStop(int stopNum) {
        if (stops.get(stopNum-1)==null) return;
        
        GradientStop stop = stops.get(stopNum-1);
        stop.color.unbind(this);

        removeChild(stop.knob);
        stops.remove(stopNum);
        
        if (isInitialised()) positionKnobs();
    }

    @Override
    public void bindableValueChanged(Bindable<UIColor> v) {
        for (GradientStop stop : stops) {
            if (stop.color==v) stop.knob.style("color", v.getValue().clone());
        }
    }
    
    private void positionKnobs() {
        for (GradientStop stop : stops) {
            UICircle knob = stop.knob;
            float offset = (1-bounds)*0.5f;
            float sliderW = resolve(getWidth());
            float knobW = knob.resolve(knob.getWidth());
            float fullTravel = sliderW-knobW;
            
            float oL = offset * fullTravel + stop.position * (fullTravel * bounds);
            knob.style("offset-left", px(oL));
        }
    }
    
    private void setValueFromMouse(float mouseX) {
        if (draggingStop==null) return;

        float sliderW = getScreenWidth();
        float value = (mouseX-getScreenX())/sliderW;

        float lBound = 0f;
        float rBound = 1f;
        int dI = stops.indexOf(draggingStop);

        if (dI-1>=0) lBound = stops.get(dI-1).position;
        if (dI+1<stops.size()) rBound = stops.get(dI+1).position;
        value = Utils.clamp(lBound, rBound, value);
        
        draggingStop.position = value;
        if (isInitialised()) positionKnobs();
    }
    
    @Override
    public void gotPointer(IOEvent event) {
        // Figure out if a knob was clicked
        GradientStop stop = null;
        for (GradientStop s : stops) {
            if (s.knob.contains(event.getMouseX(), event.getMouseY())) {
                stop = s;
                break;
            }
        }
        if (stop==null) return;
        draggingStop = stop;

        if (activeCallback!=null) activeCallback.implement(stop.color);
    }
    
    @Override
    public void lostPointer(IOEvent event) {
        draggingStop = null;
    }
    
    @Override
    public void handleIOEvent(IOEvent event) {
        switch (event.getEventType()) {
            case DRAG :
            setValueFromMouse(event.getMouseX());
            break;
            
            default: return;
        }
    }
}
