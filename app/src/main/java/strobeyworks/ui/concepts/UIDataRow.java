package strobeyworks.ui.concepts;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.ui.components.input.UISlider;
import strobeyworks.ui.components.input.UIValueMapper;
import strobeyworks.ui.components.input.field.UIFloatField;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UIFontManager;
import strobeyworks.ui.primitives.UIElement.UIAlignItems;
import strobeyworks.ui.primitives.UIElement.UIPositionMode;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;

public class UIDataRow extends UIRectangle {
    
    private UIRectangle valueArea;

    protected UIColor valueColor;
    protected UIFont valueFont;

    public UIDataRow(String name) {
        style("align-items", UIAlignItems.CENTER);
        style("color", UIColor.rgb(0.18f));
        
        valueFont = UIFontManager.getUIFont("RobotoMono-Medium.ttf", 18f);
        valueColor = UIColor.rgb(0.7f);
        
        valueArea = new UIRectangle();
        valueArea.style("width", pcw(0.59f))
        .style("height", pch(1f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-left", pcw(0.4f))
        .style("color", UIColor.transparent())
        .style("align-items", UIAlignItems.CENTER);
        
        UIText title = new UIText(valueFont, name);
        title.style("margin-left", px(10))
        .style("color", valueColor);
        
        addChild(title);
        addChild(valueArea);
    }

    protected UIRectangle getValueArea() {
        return this.valueArea;
    }
}
