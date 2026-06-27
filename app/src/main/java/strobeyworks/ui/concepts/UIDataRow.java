package strobeyworks.ui.concepts;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.pipeline.configs.ControlConfig.ActionControlConfig;
import strobeyworks.pipeline.configs.ControlConfig.BooleanControlConfig;
import strobeyworks.pipeline.configs.ControlConfig.FloatControlConfig;
import strobeyworks.pipeline.configs.ControlConfig.StringControlConfig;
import strobeyworks.ui.components.UIButton;
import strobeyworks.ui.components.input.UISlider;
import strobeyworks.ui.components.input.UIToggle;
import strobeyworks.ui.components.input.UIValueMapper;
import strobeyworks.ui.components.input.field.UIFloatField;
import strobeyworks.ui.components.input.field.UIStringField;
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

    private UIDataRow(String name) {
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

    public static UIDataRow floatControl(FloatControlConfig config) {
        UIDataRow row = new UIDataRow(config.name());
        
        row.style("align-items", UIAlignItems.CENTER);
        row.style("color", UIColor.rgb(0.18f));
        
        UIFloatField field = new UIFloatField(row.valueFont, config.min(), config.max(), config.precision());
        field.setMaxCharacters(10);
        field.style("width", pcw(0.3f))
        .style("height", pch(0.8f));

        field.bindTo(config.binding());
        row.getValueArea().addChild(field);
        
        if (config.slider()) {
            UISlider slider = new UISlider(UIValueMapper.normalisedFloat(config.min(), config.max()));
            slider.style("width", pcw(0.7f))
            .style("height", pch(0.8f))
            .style("margin-left", px(2));

            slider.bindTo(config.binding());
            row.getValueArea().addChild(slider);
        }
        else field.style("width", pcw(0.9f));

        return row;
    }

    public static UIDataRow stringControl(StringControlConfig config) {
        UIDataRow row = new UIDataRow(config.name());
        
        UIStringField field = new UIStringField(row.valueFont);
        field.setMaxCharacters(50);
        field.style("width", pcw(0.9f))
        .style("height", pch(0.8f));

        row.getValueArea().addChild(field);
        return row;
    }

    public static UIDataRow toggleControl(BooleanControlConfig config) {
        UIDataRow row = new UIDataRow(config.name());

        UIToggle toggle = new UIToggle(row.valueFont);
        toggle.style("width", pcw(0.3f))
        .style("height", pch(0.8f));
        
        toggle.bindTo(config.binding());
        row.getValueArea().addChild(toggle);

        return row;
    }

    public static UIDataRow actionControl(ActionControlConfig config) {
        UIDataRow row = new UIDataRow(config.name());
        
        UIButton button = new UIButton(row.valueFont, config.buttonText());
        button.style("width", pcw(0.3f))
        .style("height", pch(0.8f))
        .wantsPointer(true);

        button.onClicked(e -> config.action().run());
        button.onGotPointer(e -> button.style("color", UIColor.rgb(0.5f)));
        button.onLostPointer(e -> button.style("color", UIColor.rgb(0.3f)));
        
        row.getValueArea().addChild(button);
        return row;
    }

    public static UIDataRow boringRow(String name, String value) {
        UIDataRow row = new UIDataRow(name);

        UIText valueText = new UIText(row.valueFont, value);
        valueText.style("margin-left", px(10))
        .style("color", UIColor.rgb(0.7f));
        
        row.getValueArea().addChild(valueText);
        return row;
    }

}
