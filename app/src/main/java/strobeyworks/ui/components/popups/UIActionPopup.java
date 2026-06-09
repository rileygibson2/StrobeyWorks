package strobeyworks.ui.components.popups;

import static strobeyworks.ui.core.UILength.pbh;
import static strobeyworks.ui.core.UILength.pbw;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.ui.components.UIButton;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UIFontManager;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.utils.Vec4;

public class UIActionPopup extends UIPopup {
    
    private UISuccessCallback closedAction;

    private UIRectangle contentBox;

    public UIActionPopup() {
        style("width", pbw(0.8f));
        style("height", pbh(0.8f));
        style("position", UIPositionMode.ABSOLUTE);
        style("offset-left", pbw(0.1f));
        style("offset-top", pbh(0.1f));
        style("padding-top", px(10));
        style("padding-bottom", px(10));
        style("padding-left", px(10));
        style("padding-right", px(10));
        style("color", UIColor.black());
        style("border-color", UIColor.green());
        style("corner-radius", new Vec4(10));
        style("border-enabled", true);
        style("flow-direction", UIFlowDirection.COLUMN);

        contentBox = new UIRectangle();
        contentBox.style("width", pcw(1f))
        .style("height", pch(0.9f))
        .style("color", UIColor.red());

        // Buttons
        UIRectangle buttonLine = new UIRectangle();
        buttonLine.style("width", pcw(1f))
        .style("height", pch(0.1f))
        .style("color", UIColor.blue());

        UIFont font = UIFontManager.getUIFont("RobotoMono-Medium.ttf", 20f);
        UIButton close = new UIButton(font, "Cancel");
        close.style("width", pcw(0.5f))
        .style("height", pch(1.0f));

        UIButton accept = new UIButton(font, "Okay");
        accept.style("width", pcw(0.5f))
        .style("height", pch(1.0f));

        addChild(contentBox);
        addChild(buttonLine);
        buttonLine.addChild(close);
        buttonLine.addChild(accept);
    }

    public void addContent(UIElement contentRoot) {
        contentBox.addChild(contentRoot);
    }
}
