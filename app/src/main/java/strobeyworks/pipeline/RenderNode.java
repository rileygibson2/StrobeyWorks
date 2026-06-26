package strobeyworks.pipeline;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_TRIANGLES;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glDeleteBuffers;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

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
import strobeyworks.pipeline.configs.RenderInputConfig;
import strobeyworks.platform.ShaderManager;
import strobeyworks.utils.BindableValue;
import strobeyworks.utils.BindableValueObserver;
import strobeyworks.utils.Vec2;

public abstract class RenderNode implements BindableValueObserver<Float> {
    
    private final UUID id;
    private String typeName;
    private String shortName;
    private String customName;
    private RenderTarget renderTarget;

    private int fullQuadVAO;
    private int fullQuadVBO;

    private List<RenderInputConfig> inputConfigs;
    private int inputNodeCount;

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
        this.id = UUID.randomUUID();
        this.typeName = typeName;
        this.shortName = shortName;
        this.customName = "";

        this.outputWidth = -1;
        this.outputHeight = -1;

        this.inputConfigs = new ArrayList<>();
        inputNodeCount = 0;

        this.uiaPosition = new Vec2(0f);

        inspectorTabs = new ArrayList<>();
    }

    public void initialise(int outputWidth, int outputHeight) {
        this.outputWidth = outputWidth;
        this.outputHeight = outputHeight;
        handleOutputResize();
    }

    protected void initialiseFullQuad() {
        fullQuadVAO = glGenVertexArrays();
        fullQuadVBO = glGenBuffers();
        
        glBindVertexArray(fullQuadVAO);
        glBindBuffer(GL_ARRAY_BUFFER, fullQuadVBO);
        
        glBufferData(GL_ARRAY_BUFFER, ShaderManager.QUAD_VERTICES, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
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

    protected abstract void renderInputAdded(RenderInputConfig config);

    protected void addRenderInput(RenderInputConfig config) {
        if (config==null) return;
        if (config.node()==this) return;
        inputConfigs.add(config);
        countInputNodes();
        renderInputAdded(config);
    }

    protected void removeRenderInput(RenderInputConfig config) {
        inputConfigs.remove(config);
        countInputNodes();
    }

    private void countInputNodes() {
        Set<RenderNode> nodes = new HashSet<>();
        for (RenderInputConfig c : inputConfigs) nodes.add(c.node());
        inputNodeCount =  nodes.size();
    }

    protected Set<RenderNode> getDistinctInputNodes() {
        Set<RenderNode> nodes = new HashSet<>();
        for (RenderInputConfig c : inputConfigs) nodes.add(c.node());
        return nodes;
    }

    public int getInputNodeCount() {
        return inputNodeCount;
    }

    protected List<RenderInputConfig> getInputConfigs() {
        return inputConfigs;
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

    public void addInspectorControl(InspectorControl control, InspectorGroup group) {
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
        float defaultValue,
        boolean slider
    ) {
        return addFloatControl(name, min, max, precision, increment, defaultValue, slider, getLastInspectorGroup());
    }

    protected BindableValue<Float> addFloatControl(
        String name,
        float min,
        float max,
        int precision,
        float increment,
        float defaultValue,
        boolean slider,
        InspectorGroup group
    ) {
        BindableValue<Float> binding = BindableValue.of(defaultValue);
        addInspectorControl(
            new InspectorControl(
                new FloatControlConfig(
                    name,
                    binding,
                    min,
                    max,
                    precision,
                    increment,
                    defaultValue,
                    slider
                )
            ),
            group
        );
        return binding;
    }
    
    protected BindableValue<Boolean> addBooleanControl(String name, boolean defaultValue) {
        BindableValue<Boolean> binding = BindableValue.of(defaultValue);
        addInspectorControl(new InspectorControl(new BooleanControlConfig(name, binding, defaultValue)), getLastInspectorGroup());
        return binding;
    }

    protected void addActionControl(String name, String buttonText, Runnable action) {
        addInspectorControl(new InspectorControl(new ActionControlConfig("Resize to window", "Run", this::resizeToOutputWindow)), getLastInspectorGroup());
    }

    protected void setupControls() {
        createInspectorTab("Size");
        createInspectorGroup("Output Texture");
        
        widthControl = addFloatControl("Width", 1f, 10000, 0, 1f, 1500, false);
        heightControl = addFloatControl("Height", 1f, 10000, 0, 1f, 900, false);
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

    public String getIDString() {
        return this.id.toString();
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

    public void bindAndDrawFullScreen() {
        ShaderManager.getInstance().bindVAO(fullQuadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }

    public void cleanup() {
        if (fullQuadVAO!=0) glDeleteVertexArrays(fullQuadVAO);
        if (fullQuadVBO!=0) glDeleteBuffers(fullQuadVBO);

        if (renderTarget!=null) renderTarget.cleanup();
        handleCleanup();
    }

    protected abstract void handleCleanup();
}
