package strobeyworks.ui.logicpages;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.logger.Logger;
import strobeyworks.pipeline.ControlItem;
import strobeyworks.pipeline.ControlItem.ControlElement;
import strobeyworks.pipeline.ControlItem.ControlGroup;
import strobeyworks.pipeline.ControlItem.ControlTab;
import strobeyworks.pipeline.RenderNode;
import strobeyworks.pipeline.RenderPipelineListener;
import strobeyworks.pipeline.configs.ControlConfig;
import strobeyworks.pipeline.configs.ControlConfig.ActionControlConfig;
import strobeyworks.pipeline.configs.ControlConfig.BooleanControlConfig;
import strobeyworks.pipeline.configs.ControlConfig.FloatControlConfig;
import strobeyworks.pipeline.configs.ControlConfig.StringControlConfig;
import strobeyworks.ui.components.UIColorPicker;
import strobeyworks.ui.components.UIGradientSlider;
import strobeyworks.ui.components.popups.UIContentPopup;
import strobeyworks.ui.concepts.UIDataRow;
import strobeyworks.ui.concepts.UITab;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFontManager;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIRectFactory;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.utils.BindableValue;

public class UIInspectorPane extends UIRectangle implements RenderPipelineListener {
    
    private RenderNode node;
    private UITab mainTab;
    
    public UIInspectorPane() {
        style("padding-left", px(2));
        style("padding-right", px(2));
        style("padding-top", px(1));
        style("padding-bottom", px(1));
        style("align-content", UIAlignContent.CENTER);
        style("flow-direction", UIFlowDirection.COLUMN);
        style("color", UIColor.rgb(0.15f));
    }

    @Override
    public void outputtingNodeChanged(RenderNode node) {}

    @Override
    public void nodeControlsChanged() {
        loadRenderNode(node);
    }

    @Override
    public void pipelineLoaded() {
        loadRenderNode(null);
    }

    @Override
    public void pipelineSaved(String savedFileName) {}
    
    @Override
    public void initialise() {
        super.initialise();
        if (mainTab!=null) mainTab.setTab(0);
    }

    public void loadRenderNode(RenderNode node) {
        removeAllContentChildren();
        this.node = node;

        if(node==null) return;

        addTitleLine();
        addMainTab();
        
        for (ControlItem item : node.getControlTabs()) traverseInspectorTree(null, item);
        buildPropertiesTab();

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
        
        UIText type = new UIText(UIFontManager.getUIFont("RobotoMono-Medium.ttf", 15f), node.getLongName());
        type.style("margin-left", px(10))
        .style("margin-top", px(5))
        .style("color", UIColor.rgb(0.5f));
        
        addChild(line);
        line.addChild(title);
        line.addChild(UIRectFactory.rowGrow(1f));
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
        UIRectangle pane = UIRectFactory.fullContent_Collumn();
        pane.style("overflow-y", UIOverflowMode.SCROLL);
        mainTab.addTab("Props", pane);
        
        addHeadingLine(pane, "Identity");
        addControlRow(pane, new StringControlConfig("", "Node name", null, ""));
        addBoringDataLine(pane, "Node ID", node.getIDString().substring(0, 25));
    }
    
    private void traverseInspectorTree(UIRectangle pane, ControlItem item) {
        if (item instanceof ControlTab tab) {
            pane = UIRectFactory.fullContent_Collumn();
            pane.style("overflow-y", UIOverflowMode.SCROLL);
            
            mainTab.addTab(tab.name(), pane);
            for (ControlItem child : tab.items()) traverseInspectorTree(pane, child);
        }
        
        if (item instanceof ControlGroup group) {
            if (pane==null) Logger.throwRuntimeException("No inspector tab to add inspector group to");
            addHeadingLine(pane, group.name());
            for (ControlItem child : group.items()) traverseInspectorTree(pane, child);
        }
        
        if (item instanceof ControlElement control) {
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
        UIElement line = UIDataRow.boringRow(name, value);
        
        line.style("width", pcw(1f))
        .style("height", px(40))
        .style("margin-top", px(2));
        pane.addChild(line);
    }
    
    private void addControlRow(UIRectangle pane, ControlConfig config) {
        UIElement row = null;
        if (config instanceof FloatControlConfig c) row = UIDataRow.floatControl(c);
        else if (config instanceof BooleanControlConfig c) row = UIDataRow.toggleControl(c);
        else if (config instanceof StringControlConfig c) row = UIDataRow.stringControl(c);
        else if (config instanceof ActionControlConfig c) row = UIDataRow.actionControl(c);
        if (row==null) return;
        
        row.style("width", pcw(1f))
        .style("height", px(40))
        .style("margin-top", px(2));
        pane.addChild(row);
    }
    
    private UIContentPopup buildGradientPopup() {
        UIContentPopup popup = new UIContentPopup("-- Gradient --");
        
        UIRectangle pane = UIRectFactory.fullContent_Collumn();
        
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
