package strobeyworks.ui.logicpages;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.logger.Logger;
import strobeyworks.nodes.RenderNode;
import strobeyworks.rendernodes.InspectorItem;
import strobeyworks.rendernodes.InspectorItem.InspectorControl;
import strobeyworks.rendernodes.InspectorItem.InspectorGroup;
import strobeyworks.rendernodes.InspectorItem.InspectorTab;
import strobeyworks.rendernodes.configs.ActionControlConfig;
import strobeyworks.rendernodes.configs.BooleanControlConfig;
import strobeyworks.rendernodes.configs.ControlConfig;
import strobeyworks.rendernodes.configs.FloatControlConfig;
import strobeyworks.rendernodes.configs.StringControlConfig;
import strobeyworks.ui.components.UIColorPicker;
import strobeyworks.ui.components.UIGradientSlider;
import strobeyworks.ui.components.popups.UIContentPopup;
import strobeyworks.ui.concepts.UIActionControlRow;
import strobeyworks.ui.concepts.UIBoringDataRow;
import strobeyworks.ui.concepts.UIFloatControlRow;
import strobeyworks.ui.concepts.UIStringControlRow;
import strobeyworks.ui.concepts.UITab;
import strobeyworks.ui.concepts.UIToggleControlRow;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFontManager;
import strobeyworks.ui.core.UIRenderer;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIRectFactory;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.utils.BindableValue;

public class UIInspectorPane extends UIRectangle {
    
    private RenderNode node;
    private UITab mainTab;
    
    public UIInspectorPane() {
        build();
    }
    
    private void build() {
        UIRenderer ui = UIRenderer.getInstance();
        
        style("padding-left", px(2));
        style("padding-right", px(2));
        style("padding-top", px(1));
        style("padding-bottom", px(1));
        style("align-content", UIAlignContent.CENTER);
        style("flow-direction", UIFlowDirection.COLUMN);
        style("color", UIColor.rgb(0.15f));
        
        node = UIRenderer.getInstance().getSelectedNode();
        
        addTitleLine();
        addMainTab();
        
        for (InspectorItem item : node.getInspectorItems()) traverseInspectorTree(null, item);
        
        buildPropertiesTab();
    }
    
    @Override
    public void initialise() {
        super.initialise();
        mainTab.setTab(0);
    }
    
    private void addTitleLine() {
        UIRectangle line = UIRectFactory.sized(pcw(1f), px(35));
        line.style("align-items", UIAlignItems.CENTER)
        .style("color", UIColor.rgb(0.12f))
        .style("padding-left", px(10))
        .style("padding-right", px(10));
        
        UIText title = new UIText(UIFontManager.getUIFont("RobotoMono-Medium.ttf", 20f), node.getCustomName());
        title.style("color", UIColor.rgb(0.7f))
        .style("margin-top", px(5));
        
        UIText type = new UIText(UIFontManager.getUIFont("RobotoMono-Medium.ttf", 15f), node.getNodeTypeName());
        type.style("margin-left", px(10))
        .style("margin-top", px(5))
        .style("color", UIColor.rgb(0.5f));
        
        addChild(line);
        line.addChild(title);
        line.addChild(UIRectFactory.rowGrow());
        line.addChild(type);
    }
    
    private void addMainTab() {
        mainTab = new UITab(UIFontManager.getUIFont("RobotoMono-Medium.ttf", 18f));
        mainTab.style("width", pcw(1.0f))
        .style("height", px(0))
        .style("grow", 1f);
        
        addChild(mainTab);
    }
    
    private void buildPropertiesTab() {
        UIRectangle pane = UIRectFactory.fullContentCollumn();
        pane.style("overflow-y", UIOverflowMode.SCROLL);
        mainTab.addTab("Properties", pane);
        
        addHeadingLine(pane, "Identity");
        addControlRow(pane, new StringControlConfig("Node name", null, ""));
        addBoringDataLine(pane, "Node ID", node.getID().substring(0, 25));
    }
    
    private void traverseInspectorTree(UIRectangle pane, InspectorItem item) {
        if (item instanceof InspectorTab tab) {
            pane = UIRectFactory.fullContentCollumn();
            pane.style("overflow-y", UIOverflowMode.SCROLL);
            
            mainTab.addTab(tab.name(), pane);
            for (InspectorItem child : tab.items()) traverseInspectorTree(pane, child);
        }
        
        if (item instanceof InspectorGroup group) {
            if (pane==null) Logger.throwRuntimeException("No inspector tab to add inspector group to");
            addHeadingLine(pane, group.name());
            for (InspectorItem child : group.items()) traverseInspectorTree(pane, child);
        }
        
        if (item instanceof InspectorControl control) {
            if (pane==null) Logger.throwRuntimeException("No tab to add inspector control to");
            addControlRow(pane, control.config());
        }
    }
    
    private void addHeadingLine(UIRectangle pane, String name) {
        UIRectangle line = UIRectFactory.sized(pcw(1f), px(40));
        line.style("margin-top", px(2))
        .style("align-items", UIAlignItems.CENTER)
        .style("color", UIColor.rgb(0.23f));
        
        UIText title = new UIText(UIFontManager.getUIFont("RobotoMono-Medium.ttf", 18f), name);
        title.style("margin-left", px(10))
        .style("color", UIColor.rgb(0.7f));
        
        pane.addChild(line);
        line.addChild(title);
    }
    
    private void addBoringDataLine(UIRectangle pane, String name, String value) {
        UIElement line = new UIBoringDataRow(name, value);
        
        line.style("width", pcw(1f))
        .style("height", px(40))
        .style("margin-top", px(2));
        pane.addChild(line);
    }
    
    private void addControlRow(UIRectangle pane, ControlConfig config) {
        UIElement row = null;
        if (config instanceof FloatControlConfig c) row = new UIFloatControlRow(c);
        else if (config instanceof BooleanControlConfig c) row = new UIToggleControlRow(c);
        else if (config instanceof StringControlConfig c) row = new UIStringControlRow(c);
        else if (config instanceof ActionControlConfig c) row = new UIActionControlRow(c);
        if (row==null) return;
        
        row.style("width", pcw(1f))
        .style("height", px(40))
        .style("margin-top", px(2));
        pane.addChild(row);
    }
    
    private UIContentPopup buildGradientPopup() {
        UIContentPopup popup = new UIContentPopup("-- Gradient --");
        
        UIRectangle pane = UIRectFactory.fullContentCollumn();
        
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
