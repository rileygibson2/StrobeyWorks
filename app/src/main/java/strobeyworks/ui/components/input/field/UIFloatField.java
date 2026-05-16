package strobeyworks.ui.components.input.field;

import strobeyworks.ui.components.UIButton;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.style.StyleProps;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.utils.Vec4;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.pbh;
import static strobeyworks.ui.core.UILength.pbw;

public class UIFloatField extends UIField<Float> {

    UIRectangle controlWrapper;
    private UIButton up;
    private UIButton down;

    private float externalSpaceIncrement
    ;

    public UIFloatField(UIFont font, UIFloatFieldRule inputRule) {
        super(font, inputRule);

        wrapper.style("width", pcw(0.7f));
        
        controlWrapper = new UIRectangle();
        controlWrapper.style("width", pcw(0.2f))
        .style("height", pch(0.8f));
        //controlWrapper.color(col(UIColors.WHITE));

        UIStyle style = new UIStyle();
        style.set(StyleProps.ICON_TINT, col(UIColors.WHITE))
        .set(StyleProps.TRANSFORM_SCALEX, 1.2f)
        .set(StyleProps.TRANSFORM_SCALEY, 1.2f);

        up = new UIButton();
        up.style("width", pbw(1f))
        .style("height", pbh(0.5f))
        .style("corner-radius", new Vec4(0f))
        .style("border-enabled", false)
        .style("position", UIPositionMode.ABSOLUTE)
        .style("visible", false);

        up.icon("up_arrow")
        .clickedAction(() -> {increment(1);})
        .hoverStyle(style);

        down = new UIButton();
        down.style("width", pbw(1f))
        .style("height", pbh(0.5f))
        .style("corner-radius", new Vec4(0f))
        .style("border-enabled", false)
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-top", pbh(0.5f))
        .style("visible", false);

        down.icon("down_arrow")
        .clickedAction(() -> {increment(-1);})
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
