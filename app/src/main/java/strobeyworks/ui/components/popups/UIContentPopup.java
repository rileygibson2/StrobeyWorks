package strobeyworks.ui.components.popups;

import static strobeyworks.ui.core.UILength.pbh;
import static strobeyworks.ui.core.UILength.pbw;
import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.pph;
import static strobeyworks.ui.core.UILength.ppw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.ui.components.UIButton;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UIFontManager;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.utils.Vec4;

public class UIContentPopup extends UIPopup {
    
    private UIRectangle contentBox;
    
    public UIContentPopup(String title) {
        style("width", pbw(0.9f));
        style("height", pbh(0.9f));
        style("position", UIPositionMode.ABSOLUTE);
        style("offset-left", pbw(0.05f));
        style("offset-top", pbh(0.05f));
        style("padding-top", px(10));
        style("padding-bottom", px(10));
        style("padding-left", px(10));
        style("padding-right", px(10));
        style("color", UIColor.black());
        style("border-color", UIColor.green());
        style("corner-radius", new Vec4(10));
        style("border-enabled", true);
        style("flow-direction", UIFlowDirection.COLUMN);
        
        // Title Line
        UIRectangle titleLine = new UIRectangle();
        titleLine.style("width", ppw(1f))
        .style("height", pph(0.1f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("color", UIColor.transparent())
        .style("border-color", UIColor.green())
        .style("corner-radius", new Vec4(8))
        .style("border-enabled", true)
        .style("align-items", UIAlignItems.CENTER);
        
        UIFont font = UIFontManager.getUIFont("RobotoMono-Medium.ttf", 20f);
        UIText text = new UIText(font, title);
        text.style("color", UIColor.green())
        .style("margin-left", px(15));
        
        UIButton close = new UIButton("close");
        close.style("width", pcw(0.06f))
        .style("height", pch(1f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-left", pcw(0.9f))
        .style("border-enabled", false);
        close.onClicked(e -> close(false));
        
        // Content box
        contentBox = new UIRectangle();
        contentBox.style("width", pcw(1f))
        .style("height", pch(0.9f))
        .style("margin-top", pbh(0.1f));
        
        
        titleLine.addChild(close);
        titleLine.addChild(text);
        addChild(titleLine);
        addChild(contentBox);
    }
    
    public void addContent(UIElement contentRoot) {
        contentBox.addChild(contentRoot);
    }
}
