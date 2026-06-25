package strobeyworks.pipeline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import strobeyworks.SWMain;
import strobeyworks.pipeline.InspectorItem.InspectorControl;
import strobeyworks.pipeline.InspectorItem.InspectorGroup;
import strobeyworks.pipeline.InspectorItem.InspectorTab;
import strobeyworks.pipeline.configs.ActionControlConfig;
import strobeyworks.pipeline.configs.BooleanControlConfig;
import strobeyworks.pipeline.configs.FloatControlConfig;
import strobeyworks.utils.BindableValue;
import strobeyworks.utils.BindableValueObserver;
import strobeyworks.utils.Vec2;

public abstract class RenderNode implements BindableValueObserver<Float> {
    
    private final String id;
    private String typeName;
    private String shortName;
    private String customName;
    private RenderTarget renderTarget;

    private Set<RenderNode> nodeInputs;

    private int outputWidth;
    private int outputHeight;

    private List<InspectorTab> inspectorTabs;

    private BindableValue<Float> widthControl;
    private BindableValue<Float> heightControl;

    private Vec2 uiaPosition;

    private boolean syncingOutputSizeControls;
    private Integer pendingOutputWidth;
    private Integer pendingOutputHeight;

    public RenderNode(String typeName, String shortName) {
        this.id = UUID.randomUUID().toString();
        this.typeName = typeName;
        this.shortName = shortName;
        this.customName = "";

        this.outputWidth = -1;
        this.outputHeight = -1;

        this.nodeInputs = new HashSet<>();

        this.uiaPosition = new Vec2(0f);

        inspectorTabs = new ArrayList<>();
    }

    public void initialise(int outputWidth, int outputHeight) {
        this.outputWidth = outputWidth;
        this.outputHeight = outputHeight;
        handleOutputResize();
    }

    @Override
    public void bindableValueChanged(BindableValue<Float> v) {
        if (syncingOutputSizeControls) return;
        
        int newWidth = getOutputWidth();
        int newHeight = getOutputHeight();
        
        if (v==widthControl) newWidth = widthControl.getValue().intValue();
        if (v==heightControl) newHeight = heightControl.getValue().intValue();
        
        pendingOutputWidth = newWidth;
        pendingOutputHeight = newHeight;
    }
    
    public abstract void render();

    public void setRenderTarget(RenderTarget renderTarget) {
        this.renderTarget = renderTarget;
    }

    public RenderTarget getRenderTarget() {
        return renderTarget;
    }

    protected void addInputNode(RenderNode inputNode) {
        if (inputNode==this) return;
        nodeInputs.add(inputNode);
    }

    protected void removeInputNode(RenderNode inputNode) {
        nodeInputs.remove(inputNode);
    }

    protected Set<RenderNode> getInputNodes() {
        return Set.copyOf(nodeInputs);
    }

    public InspectorTab createInspectorTab(String name) {
        InspectorTab tab = new InspectorTab(name);
        inspectorTabs.add(tab);
        return tab;
    }

    public InspectorGroup createInspectorGroup(String name) {
        InspectorTab tab = getLastInspectorTab();
        if (tab==null) return null;

        InspectorGroup group = new InspectorGroup(name);
        tab.add(group);
        return group;
    }

    public void addInspectorControl(InspectorControl control) {
        InspectorGroup group = getLastInspectorGroup();
        if (group==null) return;
        group.add(control);
    }

    public InspectorTab getLastInspectorTab() {
        if (inspectorTabs.isEmpty()) return null;
        return inspectorTabs.get(inspectorTabs.size()-1);
    }

    public InspectorGroup getLastInspectorGroup() {
        InspectorTab tab = getLastInspectorTab();
        if (tab==null) return null;
        List<InspectorGroup> tabGroups = tab.items();
        if (tabGroups.isEmpty()) return null;
        return tabGroups.get(tabGroups.size()-1);
    }

    public List<InspectorItem> getInspectorTabs() {
        return List.copyOf(inspectorTabs);
    }

    protected BindableValue<Float> addFloatControl(
        String name,
        float min,
        float max,
        int precision,
        float increment,
        float defaultValue
    ) {
        BindableValue<Float> binding = BindableValue.of(defaultValue);
        addInspectorControl(new InspectorControl(new FloatControlConfig(name, binding, min, max, precision, increment, defaultValue)));
        return binding;
    }
    
    protected BindableValue<Boolean> addBooleanControl(String name, boolean defaultValue) {
        BindableValue<Boolean> binding = BindableValue.of(defaultValue);
        addInspectorControl(new InspectorControl(new BooleanControlConfig(name, binding, defaultValue)));
        return binding;
    }

    protected void addActionControl(String name, String buttonText, Runnable action) {
        addInspectorControl(new InspectorControl(new ActionControlConfig("Resize to window", "Run", this::resizeToOutputWindow)));
    }

    protected void setupControls() {
        createInspectorTab("Sizing");
        createInspectorGroup("Output Size");
        
        widthControl = addFloatControl("Width", 1f, 10000, 0, 1f, 1500);
        heightControl = addFloatControl("Height", 1f, 10000, 0, 1f, 900);
        addActionControl("Resize to window", "Run", this::resizeToOutputWindow);
        
        widthControl.bind(this);
        heightControl.bind(this);
    }

    public void loadControlDefaults() {
    }

    public void resizeOutput(int outputWidth, int outputHeight) {
        this.outputWidth = outputWidth;
        this.outputHeight = outputHeight;
        
        if (renderTarget!=null) renderTarget.resize(outputWidth, outputHeight);
        handleOutputResize();
    }

    protected void handleOutputResize() {
        syncingOutputSizeControls = true;
        try {
            widthControl.setValue((float) getOutputWidth());
            heightControl.setValue((float) getOutputHeight());
        } finally {
            syncingOutputSizeControls = false;
        }
    }

    public final void update() {
        applyPendingResize();
    }

    private void applyPendingResize() {
        if (pendingOutputWidth==null||pendingOutputHeight==null) return;
        
        resizeOutput(pendingOutputWidth, pendingOutputHeight);
        
        pendingOutputWidth = null;
        pendingOutputHeight = null;
    }

    public void resizeToOutputWindow() {
        pendingOutputWidth = SWMain.getOutputWindow().getFramebufferWidth();
        pendingOutputHeight = SWMain.getOutputWindow().getFramebufferHeight();
    }

    public void setCustomName(String name) {
        this.customName = name;
    }

    public void setUIAPosition(Vec2 position) {
        this.uiaPosition = Vec2.of(position);
    }

    public int getOutputWidth() {
        return outputWidth;
    }

    public int getOutputHeight() {
        return outputHeight;
    }

    public String getID() {
        return this.id;
    }

    public String getTypeName() {
        return this.typeName;
    }

    public String getShortName() {
        return this.shortName;
    }

    public String getCustomName() {
        return this.customName;
    }

    public Vec2 getUIAPosition() {
        return Vec2.of(uiaPosition);
    }

    public void cleanup() {
        if (renderTarget!=null) renderTarget.cleanup();
        handleCleanup();
    }

    protected abstract void handleCleanup();
}
