package strobeyworks.ui.components.input;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;

import strobeyworks.platform.IOEvent;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.primitives.UIRectFactory;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;

public class UIToggle extends UIBindableInput<Boolean, Boolean> {
    
    private UIRectangle block;
    private UIRectangle textRect;
    private UIText text;
    
    public UIToggle(UIFont font) {
        super(UIValueMapper.BOOLEAN_IDENTITY);
        
        style("align-items", UIAlignItems.CENTER);
        style("color", UIColor.rgb(0.3f));
        clickable(true);
        
        block = UIRectFactory.sized(pcw(0.5f), pch(1.0f));
        block.style("position", UIPositionMode.ABSOLUTE);

        textRect = UIRectFactory.sized(pcw(0.5f), pch(1.0f));
        textRect.style("position", UIPositionMode.ABSOLUTE)
        .style("justify-content", UIJustifyContent.CENTER)
        .style("align-items", UIAlignItems.CENTER)
        .style("offset-left", pcw(0.5f));

        text = new UIText(font);
        text.style("color", UIColor.rgb(0.7f));

        addChild(block);
        addChild(textRect);
        textRect.addChild(text);
    }
    
    @Override
    protected Boolean getDefaultLocalValue() {
        return false;
    }
    
    @Override
    protected void implementLocalValueOnUI() {
        if (getLocalValue()) {
            block.style("offset-left", pcw(0.5f))
            .style("color", UIColor.rgb(0.55f));

            textRect.style("offset-left", pcw(0f));
            text.setText("On");
        }
        else {
            block.style("offset-left", pcw(0f))
            .style("color", UIColor.rgb(0.4f));

            textRect.style("offset-left", pcw(0.5f));
            text.setText("Off");
        }
    }
    
    @Override
    public void clicked(IOEvent event) {
        setLocalValue(!getLocalValue());
        commitLocalValue();
    }
}
