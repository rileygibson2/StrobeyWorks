package strobeyworks.ui.components.input.field;

import strobeyworks.ui.components.UIButton;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.style.PrimitiveStyles;
import strobeyworks.ui.style.UIStyle;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.pbh;
import static strobeyworks.ui.core.UILength.pbw;
import static strobeyworks.ui.core.UILength.px;

public class UIFloatField extends UIField<Float> {

    UIRectangle controlWrapper;
    private UIButton up;
    private UIButton down;

    private float externalSpaceIncrement
    ;

    public UIFloatField(UILength width, UILength height, UIFont font, UIFloatFieldRule inputRule) {
        super(width, height, font, inputRule);

        wrapper.width(pcw(0.7f));
        controlWrapper = new UIRectangle(pcw(0.2f), pch(0.8f));
        //controlWrapper.color(col(UIColors.WHITE));

        UIStyle style = new UIStyle();
        style.set(PrimitiveStyles.ICON_TINT, col(UIColors.WHITE));

        up = new UIButton(pbw(1f), pbh(0.5f));
        up.icon("up_arrow")
        .clickedAction(() -> {increment(1);})
        .borderEnabled(false)
        .cornerRadius(0f)
        //.color(col(UIColors.WHITE))
        .position(UIPositionMode.ABSOLUTE)
        .visible(false)
        .hoverStyle(style);

        down = new UIButton(pbw(1f), pbh(0.5f));
        down.icon("down_arrow")
        .clickedAction(() -> {increment(-1);})
        .borderEnabled(false)
        .cornerRadius(0f)
        //.color(col(UIColors.RED))
        .position(UIPositionMode.ABSOLUTE)
        .offsetTop(pbh(0.5f))
        .visible(false)
        .hoverStyle(style);

        addChild(controlWrapper);
        controlWrapper.addChild(up);
        controlWrapper.addChild(down);
    }

    public UIFloatField useButtons(float externalSpaceIncrement) {
        this.externalSpaceIncrement = externalSpaceIncrement;
        up.visible(true);
        down.visible(true);
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
