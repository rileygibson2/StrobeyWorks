package strobeyworks.ui.concepts;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.rendernodes.configs.FloatControlConfig;
import strobeyworks.ui.components.input.UISlider;
import strobeyworks.ui.components.input.UIValueMapper;
import strobeyworks.ui.components.input.field.UIFloatField;
import strobeyworks.ui.core.UIColor;

public class UIFloatControlRow extends UIDataRow {
    
    public UIFloatControlRow(FloatControlConfig config) {
        super(config.name());

        style("align-items", UIAlignItems.CENTER);
        style("color", UIColor.rgb(0.18f));
        
        UIFloatField field = new UIFloatField(valueFont, config.min(), config.max(), config.precision());
        field.setMaxCharacters(10);
        field.style("width", pcw(0.3f))
        .style("height", pch(0.8f));
        
        UISlider slider = new UISlider(UIValueMapper.normalisedFloat(config.min(), config.max()));
        slider.style("width", pcw(0.7f))
        .style("height", pch(0.8f))
        .style("margin-left", px(2));
        
        field.bindTo(config.binding());
        slider.bindTo(config.binding());
        
        getValueArea().addChild(field);
        getValueArea().addChild(slider);
    }
}
