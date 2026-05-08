package strobeyworks.ui.components.input;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.pbh;
import static strobeyworks.ui.core.UILength.pbw;
import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;

import strobeyworks.platform.IOEvent;
import strobeyworks.platform.IOSubscriber;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.primitives.UICircle;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIElement.UIPositionMode;
import strobeyworks.ui.primitives.UIRectangle;

public class UICheckBox extends UIValueControl<Boolean, Boolean> {
    
    private UIElement inner;
    
    public UICheckBox(UILength width, UILength height, boolean circular) {
        super(width, height, UIValueAdaptor.BOOLEAN_IDENTITY);
        
        clickable(true);
        
        box(UIBoxMode.FIXED);
        flowDirection(UIFlowDirection.ROW);
        alignItems(UIAlignItems.CENTER);
        borderEnabled(true);
        borderColor(col(UIColors.GREEN));
        color(col(UIColors.TRANSPARENT));
        cornerRadius(10f);
        
        // Inner
        if (circular) {
            borderColor(col(UIColors.TRANSPARENT));
            
            UICircle c = new UICircle(pbw(1f), pbh(1f));
            c.borderEnabled(true)
            .borderColor(col(UIColors.GREEN))
            .color(col(UIColors.TRANSPARENT))
            .position(UIPositionMode.ABSOLUTE);
            
            inner = new UICircle(pcw(0.8f), pch(0.8f));
            ((UICircle) inner).color(col(UIColors.GREEN))
            .position(UIPositionMode.ABSOLUTE)
            .offsetLeft(pcw(0.1f))
            .offsetTop(pch(0.1f));
            
            addChild(c);
            addChild(inner);
        }
        else {
            inner = new UIRectangle(pcw(0.8f), pch(0.8f));
            ((UIRectangle) inner).color(col(UIColors.GREEN))
            .cornerRadius(10f)
            .position(UIPositionMode.ABSOLUTE)
            .offsetLeft(pcw(0.1f))
            .offsetTop(pch(0.1f));
            
            addChild(inner);
        }
    }
    
    @Override
    protected Boolean getDefaultLocalValue() {
        return false;
    }
    
    @Override
    protected void implementLocalValueOnUI() {
        inner.visible(getLocalValue());
    }
    
    @Override
    public void clicked(IOEvent event) {
        setLocalValue(!getLocalValue());
        commitLocalValue();
    }
}
