package strobeyworks.ui.concepts;

import static strobeyworks.ui.core.UILength.px;

import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.primitives.UIText;

public class UIBoringDataRow extends UIDataRow {
    
    public UIBoringDataRow(String name, String value) {
        super(name);

        UIText valueText = new UIText(valueFont, value);
        valueText.style("margin-left", px(10))
        .style("color", UIColor.rgb(0.7f));
        
        getValueArea().addChild(valueText);
    }
}
