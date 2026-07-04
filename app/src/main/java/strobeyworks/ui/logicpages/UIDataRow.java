package strobeyworks.ui.logicpages;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.pipeline.controls.ControlConfig;
import strobeyworks.pipeline.controls.ControlConfig.ActionControlConfig;
import strobeyworks.pipeline.controls.ControlConfig.FloatControlConfig;
import strobeyworks.pipeline.controls.ControlConfig.StringControlConfig;
import strobeyworks.pipeline.controls.ControlElement;
import strobeyworks.pipeline.controls.ControlElement.ActionControlElement;
import strobeyworks.pipeline.controls.ControlElement.DisplayControlElement;
import strobeyworks.pipeline.controls.ControlElement.InputControlElement;
import strobeyworks.pipeline.controls.ControlElement.LocalControlElement;
import strobeyworks.pipeline.input.BooleanConstantInput;
import strobeyworks.pipeline.input.FloatConstantInput;
import strobeyworks.pipeline.input.RenderInput;
import strobeyworks.pipeline.input.RenderInputParser;
import strobeyworks.pipeline.input.RenderInputSlot;
import strobeyworks.pipeline.input.SelectConstantInput;
import strobeyworks.pipeline.input.TextureInput;
import strobeyworks.platform.Transition;
import strobeyworks.ui.components.UIButton;
import strobeyworks.ui.components.input.UIDropDown;
import strobeyworks.ui.components.input.UISlider;
import strobeyworks.ui.components.input.UIToggle;
import strobeyworks.ui.components.input.UIValueMapper;
import strobeyworks.ui.components.input.UIValueMapper.UIMapResult;
import strobeyworks.ui.components.input.field.UIFloatField;
import strobeyworks.ui.components.input.field.UIFloatFieldMapper;
import strobeyworks.ui.components.input.field.UIStringField;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UIFontManager;
import strobeyworks.ui.core.UIRenderer;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.utils.BindableValue;

public class UIDataRow extends UIRectangle {
    
    private ControlElement controlElement;
    private ControlConfig<?> config;
    
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
    
    public UIDataRow(ControlConfig<?> config) {
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
        
        if (controlElement==null) {
            configureLocalControl(null);
        }
        else if (controlElement instanceof InputControlElement inputControl) {
            configureInputControl(inputControl);
        }
        else if (controlElement instanceof LocalControlElement<?> localControl) {
            configureLocalControl(localControl);
        }
        else if (controlElement instanceof ActionControlElement actionControl) {
            configureActionLocal(actionControl);
        }
        else if (controlElement instanceof DisplayControlElement displayControl) {
            configureDisplayLocal(displayControl);
        }
    }
    
    private void configureInputControl(InputControlElement controlElement) {
        RenderInputSlot slot = controlElement.getRenderInputSlot();
        if (slot == null) return;
        
        RenderInput input = slot.getInput();
        if (input == null) return;
        
        if (input instanceof FloatConstantInput) configureFloatInput(controlElement);
        else if (input instanceof BooleanConstantInput) configureBooleanInput(controlElement);
        else if (input instanceof SelectConstantInput) configureSelectInput(controlElement);
        else if (input instanceof TextureInput) configureTextureInput(controlElement);
    }
    
    private void configureLocalControl(LocalControlElement<?> controlElement) {
        if (controlElement==null&&this.config==null) return;

        ControlConfig<?> config = this.config;
        if (controlElement!=null) config = controlElement.getConfig();
        
        if (config instanceof FloatControlConfig) {
            @SuppressWarnings("unchecked")
            LocalControlElement<Float> floatControl = (LocalControlElement<Float>) controlElement;
            configureFloatLocal(floatControl);
        }
        else if (config instanceof StringControlConfig) {
            @SuppressWarnings("unchecked")
            LocalControlElement<String> stringControl = (LocalControlElement<String>) controlElement;
            configureStringLocal(stringControl);
        }
    }
    
    public void configureFloatInput(InputControlElement controlElement) {
        FloatControlConfig config = (FloatControlConfig) controlElement.getConfig();
        FloatConstantInput input = (FloatConstantInput) controlElement.getRenderInputSlot().getInput();
        BindableValue<Float> binding = input.getBinding();
        
        UIStringField field = new UIStringField(valueFont);
        field.setMaxCharacters(50);
        field.style("width", pcw(0.3f))
        .style("height", pch(0.8f));
        
        field.setLocalValue(getDisplayStringForInput(controlElement));
        field.onCommit(() -> triggerParse(field));
        valueArea.addChild(field);
        
        if (config.slider()) {
            UISlider slider = new UISlider(UIValueMapper.normalisedFloat(config.min(), config.max()));
            slider.style("width", pcw(0.7f))
            .style("height", pch(0.8f))
            .style("margin-left", px(2));
            
            slider.bindTo(binding);
            valueArea.addChild(slider);
            
            slider.onLostPointer(e -> {
                field.setLocalValue(getDisplayStringForInput(controlElement));
            });
        }
        else field.style("width", pcw(0.9f));
    }
    
    public void configureBooleanInput(InputControlElement controlElement) {
        BooleanConstantInput input = (BooleanConstantInput) controlElement.getRenderInputSlot().getInput();
        BindableValue<Boolean> binding = input.getBinding();
        
        UIToggle toggle = new UIToggle(valueFont);
        toggle.style("width", pcw(0.3f))
        .style("height", pch(0.8f));
        
        toggle.bindTo(binding);
        valueArea.addChild(toggle);
    }

    public void configureSelectInput(InputControlElement controlElement) {
        SelectConstantInput input = (SelectConstantInput) controlElement.getRenderInputSlot().getInput();
        BindableValue<Integer> binding = input.getBinding();
        
        UIDropDown drop = new UIDropDown(valueFont, input.getOptions());
        drop.style("width", pcw(1f))
        .style("height", pch(0.8f));
        
        drop.bindTo(binding);
        valueArea.addChild(drop);
    }
    
    public void configureTextureInput(InputControlElement controlElement) {
        TextureInput input = (TextureInput) controlElement.getRenderInputSlot().getInput();
        
        UIStringField field = new UIStringField(valueFont);
        field.setLocalValue(input.getString());
        field.setMaxCharacters(50);
        field.style("width", pcw(1f))
        .style("height", pch(0.8f));
        
        field.onCommit(() -> triggerParse(field));
        valueArea.addChild(field);
    }
    
    public void configureFloatLocal(LocalControlElement<Float> controlElement) {
        FloatControlConfig config = (FloatControlConfig) controlElement.getConfig();
        BindableValue<Float> binding = controlElement.getBinding();
        
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
    
    public void configureStringLocal(LocalControlElement<String> controlElement) {
        UIStringField field = new UIStringField(valueFont);
        field.setMaxCharacters(50);
        field.style("width", pcw(0.9f))
        .style("height", pch(0.8f));
        
        valueArea.addChild(field);
    }
    
    public void configureActionLocal(ActionControlElement controlElement) {
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
    
    public void configureDisplayLocal(DisplayControlElement controlElement) {
        BindableValue<?> binding = controlElement.getBinding();
        Object rawValue = binding.getValue();
        if (rawValue==null) return;
        
        UIText valueText = new UIText(valueFont, rawValue.toString());
        valueText.style("margin-left", px(10))
        .style("color", UIColor.rgb(0.7f));
        
        valueArea.addChild(valueText);
        
        bindDisplayText(binding, valueText);
    }
    
    private <T> void bindDisplayText(BindableValue<T> binding, UIText text) {
        binding.bind(v -> {
            T value = v.getValue();
            text.setText(value == null ? "" : value.toString());
        });
    }
    
    public void configureBoringRow(String name, String value) {
        UIText valueText = new UIText(valueFont, value);
        valueText.style("margin-left", px(10))
        .style("color", UIColor.rgb(0.7f));
        
        valueArea.addChild(valueText);
    }
    
    private String getDisplayStringForInput(InputControlElement controlElement) {
        RenderInputSlot slot = controlElement.getRenderInputSlot();
        RenderInput input = slot.getInput();
        
        if (input instanceof FloatConstantInput floatInput && controlElement.getConfig() instanceof FloatControlConfig floatConfig) {
            UIFloatFieldMapper mapper = new UIFloatFieldMapper()
            .inputMinMax(floatConfig.min(), floatConfig.max())
            .maxPrecision(floatConfig.precision());
            
            UIMapResult<String> result = mapper.mapExternalToLocal(floatInput.getBinding().getValue());
            if (result.success()) return result.value();
        }
        
        return input != null ? input.getString() : "";
    }
    
    private void triggerParse(UIStringField field) {
        String text = field.getLocalValue();
        if (!(controlElement instanceof InputControlElement)) return;
        
        InputControlElement controlElement = (InputControlElement) this.controlElement;
        RenderInputSlot slot = controlElement.getRenderInputSlot();
        
        RenderInput oldInput = slot.getInput();
        
        RenderInput parsed = RenderInputParser.parse(text, slot);
        if (parsed == null) {
            field.setLocalValue(getDisplayStringForInput(controlElement));
            flashFieldError(field);
            return;
        }
        
        boolean result = controlElement.getParentNode().setSlotInput(slot, parsed);       
        if (!result) {
            field.setLocalValue(getDisplayStringForInput(controlElement));
            flashFieldError(field);
            return;
        }
        
        RenderInput newInput = slot.getInput();
        
        if (oldInput != null && newInput != null && oldInput.getClass() == newInput.getClass()) {
            field.setLocalValue(getDisplayStringForInput(controlElement));
        } else {
            configure();
        }
    }
    
    private void flashFieldError(UIStringField field) {
        UIColor normal = UIColor.transparent(); // or whatever your normal field border is
        UIColor error = UIColor.rgb(1f, 0.15f, 0.15f);
        
        field.style("border-enabled", true);
        field.style("border-color", error);
        
        Transition reset = new Transition(0f, 0.5f, "field-error-reset");
        reset.setCompletedAction(() -> {
            field.style("border-color", normal)
            .style("border-enabled", false);
        });
        
        UIRenderer.getInstance().addTransition(field, reset);
    }
}
