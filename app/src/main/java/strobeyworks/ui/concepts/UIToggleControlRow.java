package strobeyworks.ui.concepts;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;

import strobeyworks.pipeline.configs.BooleanControlConfig;
import strobeyworks.ui.components.input.UIToggle;

public class UIToggleControlRow extends UIDataRow {
    
    public UIToggleControlRow(BooleanControlConfig config) {
        super(config.name());
        
        UIToggle toggle = new UIToggle(valueFont);
        toggle.style("width", pcw(0.3f))
        .style("height", pch(0.8f));
        
        toggle.bindTo(config.binding());
        getValueArea().addChild(toggle);
    }
}
