package strobeyworks.ui.primitives;

import strobeyworks.ui.core.UILength;
import strobeyworks.ui.primitives.UIElement.UIAlignItems;
import strobeyworks.ui.primitives.UIElement.UIFlowDirection;
import strobeyworks.ui.primitives.UIElement.UIJustifyContent;
import strobeyworks.ui.primitives.UIElement.UIPositionMode;

import static strobeyworks.ui.core.UILength.px;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.pch;

public class UIRectFactory {

    public static UIRectangle sized(UILength width, UILength height) {
        UIRectangle r = new UIRectangle();
        r.style("width", width)
        .style("height", height);
        return r;
    }

    public static UIRectangle sizedCentered(UILength width, UILength height) {
        UIRectangle r = new UIRectangle();
        r.style("width", width)
        .style("height", height)
        .style("justify-content", UIJustifyContent.CENTER)
        .style("align-items", UIAlignItems.CENTER);
        return r;
    }

    public static UIRectangle sizedAligned(UILength width, UILength height) {
        UIRectangle r = new UIRectangle();
        r.style("width", width)
        .style("height", height)
        .style("align-items", UIAlignItems.CENTER);
        return r;
    }

    public static UIRectangle absolute(UILength width, UILength height) {
        UIRectangle r = new UIRectangle();
        r.style("width", width)
        .style("height", height)
        .style("position", UIPositionMode.ABSOLUTE);
        return r;
    }
    
    public static UIRectangle fullContentCollumn() {
        UIRectangle r = new UIRectangle();
        r.style("width", pcw(1.0f))
        .style("height", pch(1.0f))
        .style("flow-direction", UIFlowDirection.COLUMN);
        return r;
    }

    public static UIRectangle fullContentRow() {
        UIRectangle r = new UIRectangle();
        r.style("width", pcw(1.0f))
        .style("height", pch(1.0f))
        .style("flow-direction", UIFlowDirection.ROW);
        return r;
    }

    public static UIRectangle rowGrow() {
        UIRectangle r = new UIRectangle();
        r.style("width", px(1))
        .style("height", pch(1.0f))
        .style("grow", 1f);
        return r;
    }
}
