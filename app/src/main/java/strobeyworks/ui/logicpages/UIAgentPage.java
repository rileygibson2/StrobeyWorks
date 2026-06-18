package strobeyworks.ui.logicpages;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.pph;
import static strobeyworks.ui.core.UILength.ppw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.noiserender.AgentRenderer;
import strobeyworks.noiserender.ControlConfig;
import strobeyworks.ui.components.UIButton;
import strobeyworks.ui.components.UIColorPicker;
import strobeyworks.ui.components.UIGradientSlider;
import strobeyworks.ui.components.input.UISlider;
import strobeyworks.ui.components.input.UIValueMapper;
import strobeyworks.ui.components.input.field.UIFloatField;
import strobeyworks.ui.components.input.field.UIFloatFieldMapper;
import strobeyworks.ui.components.popups.UIContentPopup;
import strobeyworks.ui.concepts.UIValueLine;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UIFontManager;
import strobeyworks.ui.core.UIRenderer;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.utils.BindableValue;

public class UIAgentPage extends UIRectangle {
    
    public UIAgentPage() {
        build();
    }
    
    private void build() {
        UIRenderer ui = UIRenderer.getInstance();
        
        style("width", ppw(1f));
        style("height", pph(1f));
        style("padding-left", px(5));
        style("padding-right", px(5));
        style("padding-top", px(5));
        style("padding-bottom", px(5));
        style("align-content", UIAlignContent.CENTER);
        style("flow-direction", UIFlowDirection.COLUMN);
        style("color", UIColor.transparent());
        style("overflow-y", UIOverflowMode.SCROLL);
        
        AgentRenderer r = AgentRenderer.getInstance();
        UIFont fieldFont = UIFontManager.getUIFont("RobotoMono-Medium.ttf", 20f);
        
        UIRectangle line = new UIRectangle();
        line.style("width", pcw(1f))
        .style("height", pch(0.08f));
        addChild(line);
        
        UIButton but = new UIButton(fieldFont, "Popup");
        but.style("width", pcw(0.2f))
        .style("height", pch(1.0f))
        .onClicked(e -> {
            ui.createFullScreenPopup(buildGradientPopup());
        });
        line.addChild(but);
        
        but = new UIButton(fieldFont, "Restore");
        but.style("width", pcw(0.2f))
        .style("height", pch(1.0f))
        .onClicked(e -> {
            r.loadDefaults();
        });
        line.addChild(but);
        
        but = new UIButton(fieldFont, "Randomize");
        but.style("width", pcw(0.2f))
        .style("height", pch(1.0f))
        .onClicked(e -> {
            r.randomize();
        });
        line.addChild(but);
        
        line = new UIRectangle();
        line.style("width", pcw(1f))
        .style("box", UIBoxMode.FLEX)
        //.style("height", pch(0.08f))
        .style("margin-top", px(10))
        .style("max-width", pcw(1f))
        .style("align-items", UIAlignItems.CENTER)
        //.style("color", col(UIColors.RED))
        .style("flow-wrap", true);
        addChild(line);
        
        for (ControlConfig<Float> config : r.getFloatControlConfigs()) {
            line = new UIValueLine(config);
            line.style("width", pcw(1f))
            .style("height", pch(0.1f))
            .style("margin-top", px(1));
            addChild(line);
        }
    }
    
    private UIContentPopup buildGradientPopup() {
        UIContentPopup popup = new UIContentPopup("-- Gradient --");
        
        UIRectangle pane = UIRectangle.fullContentCollumn();
        
        UIColorPicker colPick = new UIColorPicker();
        colPick.style("width", pcw(1f))
        .style("height", pch(0.5f));
        
        UIGradientSlider gradSlider = new UIGradientSlider();
        gradSlider.style("width", pcw(1f))
        .style("height", pch(0.1f))
        .style("margin-top", px(10));
        
        BindableValue<UIColor> s1 = BindableValue.of(UIColor.red());
        BindableValue<UIColor> s2 = BindableValue.of(UIColor.green());
        BindableValue<UIColor> s3 = BindableValue.of(UIColor.blue());
        
        gradSlider.addStop(s1, 0.1f);
        gradSlider.addStop(s2, 0.3f);
        gradSlider.addStop(s3, 0.5f);
        
        gradSlider.setActiveCallback(c -> {
            colPick.bindColor(c);
        });
        
        
        pane.addChild(colPick);
        pane.addChild(gradSlider);
        
        popup.addContent(pane);
        
        return popup;
    }
}
