package strobeyworks.ui.logicpages;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.logger.Logger;
import strobeyworks.pipeline.controls.ControlConfig.ActionControlConfig;
import strobeyworks.pipeline.controls.ControlConfig.BooleanControlConfig;
import strobeyworks.pipeline.controls.ControlConfig.DisplayControlConfig;
import strobeyworks.pipeline.controls.ControlConfig.FloatControlConfig;
import strobeyworks.pipeline.controls.ControlConfig.StringControlConfig;
import strobeyworks.pipeline.controls.ControlConfig;
import strobeyworks.pipeline.controls.ControlElement;
import strobeyworks.pipeline.input.BooleanConstantInput;
import strobeyworks.pipeline.input.FloatConstantInput;
import strobeyworks.pipeline.input.RenderInput;
import strobeyworks.pipeline.input.RenderInputParser;
import strobeyworks.pipeline.input.RenderInputSlot;
import strobeyworks.pipeline.input.TextureInput;
import strobeyworks.pipeline.input.TextureInput.TextureInputMode;
import strobeyworks.ui.components.UIButton;
import strobeyworks.ui.components.input.UISlider;
import strobeyworks.ui.components.input.UIToggle;
import strobeyworks.ui.components.input.UIValueMapper;
import strobeyworks.ui.components.input.field.UIFloatField;
import strobeyworks.ui.components.input.field.UIStringField;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UIFontManager;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.utils.BindableValue;

public class UIDataRow extends UIRectangle {
    
    private ControlElement controlElement;
    private ControlConfig config;
    
    private UIRectangle valueArea;
    protected UIColor valueColor;
    protected UIFont valueFont;
    protected UIFont valueFontSmall;
    
    public UIDataRow(ControlElement controlElement) {
        this.controlElement = controlElement;
        this.config = controlElement.getConfig();
        
        configureBase(controlElement.getName());
        configure();
    }
    
    public UIDataRow(ControlConfig config) {
        this.config = config;
        
        configureBase(config.name());
        configure();
    }
    
    public UIDataRow(String name, String value) {
        configureBase(name);
        configureBoringRow(name, value);
    }
    
    private void configureBase(String name) {
        style("align-items", UIAlignItems.CENTER);
        style("color", UIColor.rgb(0.18f));
        
        valueFont = UIFontManager.getUIFont("RobotoMono-Medium.ttf", 18f);
        valueFontSmall = UIFontManager.getUIFont("RobotoMono-Medium.ttf", 12f);
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
    
    private void configure() {
        valueArea.removeAllContentChildren();

        if (controlElement!=null) {
            RenderInputSlot slot = controlElement.getRenderInputSlot();
            if (slot==null) return;
            RenderInput input = slot.getInput();
            if (input==null) return;

            if (input instanceof FloatConstantInput) configureFloatInput();
            else if (input instanceof BooleanConstantInput) configureBooleanInput();
            else if (input instanceof TextureInput) configureTextureInput();
            return;
        }
        
        if (config instanceof FloatControlConfig) configureFloatControl();
        else if (config instanceof StringControlConfig) configureStringControl();
        else if (config instanceof ActionControlConfig) configureActionControl();
        else if (config instanceof DisplayControlConfig) configureDisplayControl();
    }
    
    public void configureFloatInput() {
        FloatControlConfig config = (FloatControlConfig) controlElement.getConfig();
        FloatConstantInput input = (FloatConstantInput) controlElement.getRenderInputSlot().getInput();
        BindableValue<Float> binding = input.getBinding();
        
        UIStringField field = new UIStringField(valueFont);
        field.setLocalValue(input.getString());
        field.setMaxCharacters(50);
        field.style("width", pcw(0.3f))
        .style("height", pch(0.8f));
        
        field.onCommit(() -> triggerParse(field));
        
        valueArea.addChild(field);
        
        if (config.slider()) {
            UISlider slider = new UISlider(UIValueMapper.normalisedFloat(config.min(), config.max()));
            slider.style("width", pcw(0.7f))
            .style("height", pch(0.8f))
            .style("margin-left", px(2));
            
            slider.bindTo(binding);
            valueArea.addChild(slider);
        }
        else field.style("width", pcw(0.9f));
    }

    public void configureBooleanInput() {
        BooleanConstantInput input = (BooleanConstantInput) controlElement.getRenderInputSlot().getInput();
        BindableValue<Boolean> binding = input.getBinding();
        
        UIToggle toggle = new UIToggle(valueFont);
        toggle.style("width", pcw(0.3f))
        .style("height", pch(0.8f));
        
        toggle.bindTo(binding);
        valueArea.addChild(toggle);
    }

    public void configureTextureInput() {
        TextureInput input = (TextureInput) controlElement.getRenderInputSlot().getInput();
        
        UIStringField field = new UIStringField(valueFont);
        field.setLocalValue(input.getString());
        field.setMaxCharacters(50);
        field.style("width", pcw(1f))
        .style("height", pch(0.8f));
        
        field.onCommit(() -> triggerParse(field));
        valueArea.addChild(field);
    }






    public void configureFloatControl() {
        FloatControlConfig config = (FloatControlConfig) controlElement.getConfig();
        FloatConstantInput input = (FloatConstantInput) controlElement.getRenderInputSlot().getInput();
        BindableValue<Float> binding = input.getBinding();
        
        UIFloatField field = new UIFloatField(
            valueFont,
            config.min(),
            config.max(),
            config.precision()
        );
        field.setMaxCharacters(50);
        field.style("width", pcw(0.3f))
        .style("height", pch(0.8f));
        field.bindTo(binding);
        
        valueArea.addChild(field);
        
        if (config.slider()) {
            UISlider slider = new UISlider(UIValueMapper.normalisedFloat(config.min(), config.max()));
            slider.style("width", pcw(0.7f))
            .style("height", pch(0.8f))
            .style("margin-left", px(2));
            
            slider.bindTo(binding);
            valueArea.addChild(slider);
        }
        else field.style("width", pcw(0.9f));
    }
    
    public void configureStringControl() {
        UIStringField field = new UIStringField(valueFont);
        field.setMaxCharacters(50);
        field.style("width", pcw(0.9f))
        .style("height", pch(0.8f));
        
        valueArea.addChild(field);
    }
    
    public void configureActionControl() {
        ActionControlConfig config = (ActionControlConfig) controlElement.getConfig();
        
        UIButton button = new UIButton(valueFont, config.buttonText());
        button.style("width", pcw(0.3f))
        .style("height", pch(0.8f))
        .wantsPointer(true);
        
        button.onClicked(e -> config.action().run());
        button.onGotPointer(e -> button.style("color", UIColor.rgb(0.5f)));
        button.onLostPointer(e -> button.style("color", UIColor.rgb(0.3f)));
        
        valueArea.addChild(button);
    }
    
    public void configureBoringRow(String name, String value) {
        UIText valueText = new UIText(valueFont, value);
        valueText.style("margin-left", px(10))
        .style("color", UIColor.rgb(0.7f));
        
        valueArea.addChild(valueText);
    }
    
    private void triggerParse(UIStringField field) {
        String text = field.getLocalValue();
        RenderInputSlot slot = controlElement.getRenderInputSlot();

        RenderInput parsed =  RenderInputParser.parse(text);
        Logger.debug(parsed);
        if (parsed == null) {
            Logger.debug("no parsed object found");
            return;
        }
        
        boolean result = controlElement.getParentNode().setSlotInput(slot, parsed);
        
        if (result) configure();
        else {
            // Error/invalid communication to user
        }
    }
}
