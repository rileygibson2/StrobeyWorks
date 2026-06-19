package strobeyworks.ui.concepts;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.rendernodes.ControlConfig;
import strobeyworks.ui.components.input.UISlider;
import strobeyworks.ui.components.input.UIValueMapper;
import strobeyworks.ui.components.input.field.UIFloatField;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UIFontManager;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;

public class UIValueLine extends UIRectangle {
    
    public UIValueLine(ControlConfig<Float> config) {
        style("align-items", UIAlignItems.CENTER);
        style("color", UIColor.rgb(0.2f));
        
        UIFont titleFont = UIFontManager.getUIFont("RobotoMono-Medium.ttf", 18f);
        UIFont fieldFont = UIFontManager.getUIFont("RobotoMono-Medium.ttf", 18f);
        
        UIRectangle right = new UIRectangle();
        right.style("width", pcw(0.59f))
        .style("height", pch(1f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-left", pcw(0.4f))
        .style("color", UIColor.transparent())
        .style("align-items", UIAlignItems.CENTER);
        
        UIText title = new UIText(titleFont, config.name());
        title.style("margin-left", px(10))
        .style("color", UIColor.rgb(0.7f));
        
        UIFloatField field = new UIFloatField(fieldFont, config.min(), config.max(), config.precision());
        field.setMaxCharacters(3);
        //field.useButtons(config.increment());
        field.style("width", pcw(0.2f))
        .style("height", pch(0.8f));
        
        UISlider slider = new UISlider(UIValueMapper.normalisedFloat(config.min(), config.max()));
        slider.style("width", pcw(0.8f))
        .style("height", pch(0.8f))
        .style("margin-left", px(2));
        
        field.bindTo(config.binding());
        slider.bindTo(config.binding());
        
        addChild(title);
        addChild(right);
        right.addChild(field);
        right.addChild(slider);
    }
}
