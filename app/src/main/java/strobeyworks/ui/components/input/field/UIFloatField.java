package strobeyworks.ui.components.input.field;

import static strobeyworks.ui.core.UILength.pbh;
import static strobeyworks.ui.core.UILength.pbw;
import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;

import strobeyworks.ui.components.UIButton;
import strobeyworks.ui.components.input.UIValueMapper;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.style.StyleProps;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.utils.Vec4;

public class UIFloatField extends UIField<Float> {
    
    UIRectangle controlWrapper;
    private UIButton up;
    private UIButton down;
    
    private float externalSpaceIncrement;

    public UIFloatField(UIFont font, float min, float max, int precision) {
        this(font, new UIFloatFieldMapper()
        .inputMinMax(min, max)
        .maxPrecision(precision));
    }
    
    public UIFloatField(UIFont font, UIValueMapper<Float, String> mapper) {
        super(font, mapper);
        setRegex("[0-9.]");
        
        wrapper.style("width", pcw(0.7f));
        
        controlWrapper = new UIRectangle();
        controlWrapper.style("width", pcw(0.2f))
        .style("height", pch(0.8f));
        
        UIStyle style = new UIStyle();
        style.set(StyleProps.BUTTON_ICON_TINT, UIColor.white());
        
        up = new UIButton("up_arrow");
        up.style("width", pbw(1f))
        .style("height", pbh(0.5f))
        .style("corner-radius", new Vec4(0f))
        .style("border-enabled", false)
        .style("position", UIPositionMode.ABSOLUTE)
        .style("visible", false)
        .style("button-icon-tint", UIColor.rgb(0.8f))
        .style(StyleProps.TRANSFORM_SCALEX, 1f)
        .style(StyleProps.TRANSFORM_SCALEY, 1f)
        .onClicked(e -> {increment(1);})
        .hoverStyle(style);
        
        down = new UIButton("down_arrow");
        down.style("width", pbw(1f))
        .style("height", pbh(0.5f))
        .style("corner-radius", new Vec4(0f))
        .style("border-enabled", false)
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-top", pbh(0.5f))
        .style("visible", false)
        .style("button-icon-tint", UIColor.rgb(0.8f))
        .style(StyleProps.TRANSFORM_SCALEX, 1f)
        .style(StyleProps.TRANSFORM_SCALEY, 1f)
        .onClicked(e -> {increment(-1);})
        .hoverStyle(style);
        
        addChild(controlWrapper);
        controlWrapper.addChild(up);
        controlWrapper.addChild(down);
    }
    
    public UIFloatField useButtons(float externalSpaceIncrement) {
        this.externalSpaceIncrement = externalSpaceIncrement;
        up.style("visible", true);
        down.style("visible", true);
        return this;
    }
    
    @Override
    protected String getDefaultLocalValue() {
        return "0";
    }
    
    private void increment(int direction) {
        float i = externalSpaceIncrement*direction;
        float f = previewExternalValue()+i;
        commitValue(f);
    }
}
