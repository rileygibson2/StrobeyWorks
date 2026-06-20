package strobeyworks.ui.concepts;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.rendernodes.configs.StringControlConfig;
import strobeyworks.ui.components.input.field.UIStringField;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UIFontManager;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;

public class UIStringControlRow extends UIDataRow {
    
    public UIStringControlRow(StringControlConfig config) {
        super(config.name());
        
        UIStringField field = new UIStringField(valueFont);
        field.setMaxCharacters(50);
        field.style("width", pcw(0.9f))
        .style("height", pch(0.8f));
        
        
        getValueArea().addChild(field);
    }
}
