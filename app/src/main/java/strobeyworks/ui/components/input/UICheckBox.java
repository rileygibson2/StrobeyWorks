package strobeyworks.ui.components.input;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.pph;
import static strobeyworks.ui.core.UILength.ppw;

import strobeyworks.platform.IOEvent;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.primitives.UICircle;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIElement.UIPositionMode;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.utils.Vec4;

public class UICheckBox extends UIValueControl<Boolean, Boolean> {
    
    private UIElement inner;
    
    public UICheckBox(UILength width, UILength height, boolean circular) {
        super(UIValueAdaptor.BOOLEAN_IDENTITY);
        
        style("width", width);
        style("height", height);
        clickable(true);
        
        style("box", UIBoxMode.FIXED);
        style("flow-direction", UIFlowDirection.ROW);
        style("align-items", UIAlignItems.CENTER);
        style("border-enabled", true);
        style("border-color", col(UIColors.GREEN));
        style("color", col(UIColors.TRANSPARENT));
        style("corner-radius", new Vec4(10f));
        
        // Inner
        if (circular) {
            style("border-color", col(UIColors.TRANSPARENT));
            
            UICircle c = new UICircle();
            c.style("width", ppw(1f))
            .style("height", pph(1f))
            .style("border-color", col(UIColors.GREEN))
            .style("color", col(UIColors.TRANSPARENT))
            .style("border-enabled", true)
            .style("position", UIPositionMode.ABSOLUTE);
            
            inner = new UICircle();
            inner.style("width", ppw(0.8f))
            .style("height", pph(0.8f))
            .style("color", col(UIColors.GREEN))
            .style("position", UIPositionMode.ABSOLUTE)
            .style("offset-left", ppw(0.1f))
            .style("offset-top", pph(0.1f));
            
            addChild(c);
            addChild(inner);
        }
        else {
            inner = new UIRectangle();
            inner.style("width", ppw(0.8f))
            .style("height", pph(0.8f))
            .style("color", col(UIColors.GREEN))
            .style("corner-radius", new Vec4(10f))
            .style("position", UIPositionMode.ABSOLUTE)
            .style("offset-left", ppw(0.1f))
            .style("offset-top", pph(0.1f));
            
            addChild(inner);
        }
    }
    
    @Override
    protected Boolean getDefaultLocalValue() {
        return false;
    }
    
    @Override
    protected void implementLocalValueOnUI() {
        inner.style("visible", getLocalValue());
    }
    
    @Override
    public void clicked(IOEvent event) {
        setLocalValue(!getLocalValue());
        commitLocalValue();
    }
}
