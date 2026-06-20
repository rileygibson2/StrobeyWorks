package strobeyworks.ui.concepts;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;

import strobeyworks.rendernodes.configs.ActionControlConfig;
import strobeyworks.ui.components.UIButton;
import strobeyworks.ui.core.UIColor;

public class UIActionControlRow extends UIDataRow {
    
    public UIActionControlRow(ActionControlConfig config) {
        super(config.name());
        
        UIButton button = new UIButton(valueFont, config.buttonText());
        button.style("width", pcw(0.3f))
        .style("height", pch(0.8f))
        .wantsPointer(true);

        button.onClicked(e -> config.action().run());
        button.onGotPointer(e -> button.style("color", UIColor.rgb(0.5f)));
        button.onLostPointer(e -> button.style("color", UIColor.rgb(0.3f)));
        
        getValueArea().addChild(button);
    }
}
