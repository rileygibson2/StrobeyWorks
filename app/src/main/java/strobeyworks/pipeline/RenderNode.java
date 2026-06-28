package strobeyworks.pipeline;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gson.JsonElement;

import strobeyworks.SWMain;
import strobeyworks.pipeline.ControlItem.ControlElement;
import strobeyworks.pipeline.ControlItem.ControlGroup;
import strobeyworks.pipeline.ControlItem.ControlTab;
import strobeyworks.pipeline.configs.ControlConfig;
import strobeyworks.pipeline.configs.ControlConfig.ActionControlConfig;
import strobeyworks.pipeline.configs.ControlConfig.BooleanControlConfig;
import strobeyworks.pipeline.configs.ControlConfig.FloatControlConfig;
import strobeyworks.pipeline.configs.ControlConfig.IntegerControlConfig;
import strobeyworks.pipeline.configs.ControlConfig.StringControlConfig;
import strobeyworks.pipeline.configs.TextureInput;
import strobeyworks.pipeline.configs.TextureInput.TextureInputState;
import strobeyworks.platform.ShaderManager;
import strobeyworks.rendernodes.AgentNode;
import strobeyworks.rendernodes.MaskNode;
import strobeyworks.rendernodes.MixNode;
import strobeyworks.rendernodes.PerlinNode;
import strobeyworks.utils.BindableValue;
import strobeyworks.utils.BindableValueObserver;
import strobeyworks.utils.Vec2;
import strobeyworks.utils.Vec2I;

public abstract class RenderNode implements BindableValueObserver<Float> {
    
    public static final Map<String, Class<? extends RenderNode>> NODE_TYPE_REGISTRY = Map.of(
        "Perlin-Noise", PerlinNode.class,
        "Species-Agents", AgentNode.class,
        "Mix", MixNode.class,
        "Mask", MaskNode.class
    );
    
    public record RenderNodeState(
        String id,
        String typeName,
        String customName,
        int uiaXPos,
        int uiaYPos,
        List<RenderControlState> controls,
        List<TextureInputState> textureInputs
    ) {}
    
    public record RenderControlState(
        String key,
        String type,
        JsonElement value
    ) {}
    
    private final UUID id;
    private final String longName;
    private final String shortName;
    private String customName;
    private RenderTarget renderTarget;
    
    private int fullQuadVAO;
    private int fullQuadVBO;
    
    private List<TextureInput> textureInputs;
    private final int maxTextureInputs;
    private int nodeDependancyCount;
    
    private int outputWidth;
    private int outputHeight;
    
    private List<ControlTab> controlTabs;
    
    private List<ControlConfig> registeredControls;
    
    private BindableValue<Float> widthControl;
    private BindableValue<Float> heightControl;
    
    private Vec2 uiaPosition;
    
    private boolean syncingOutputSizeControls;
    private Integer pendingOutputWidth;
    private Integer pendingOutputHeight;
    
    public RenderNode(UUID id, String longName, String shortName, int maxTextureInputs) {
        this.id = id;
        this.longName = longName;
        this.shortName = shortName;
        this.customName = "";
        
        registeredControls = new ArrayList<>();
        
        this.outputWidth = -1;
        this.outputHeight = -1;
        
        this.textureInputs = new ArrayList<>();
        this.maxTextureInputs = maxTextureInputs;
        this.nodeDependancyCount = 0;
        
        this.uiaPosition = new Vec2(0f);
        
        controlTabs = new ArrayList<>();
    }
    
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // ------ REFLECTIVE CREATION
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    
    public static <T extends RenderNode> T getNode(Class<T> nodeClass) {
        try {
            return nodeClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create render node: " + nodeClass.getName(), e);
        }
    }
    
    public static <T extends RenderNode> T getNode(Class<T> nodeClass, UUID id) {
        try {
            return nodeClass.getDeclaredConstructor(UUID.class).newInstance(id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create render node: " + nodeClass.getName(), e);
        }
    }
    
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // ------ INITIALISATION AND LISTENERS
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    
    public void initialise(Vec2I dimensions) {
        this.outputWidth = dimensions.x;
        this.outputHeight = dimensions.y;
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
    
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // ------ INPUT HANDLING
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    
    protected abstract void textureInputAdded(TextureInput input);
    
    protected void addTextureInput(TextureInput input) {
        if (input==null) return;
        if (input.getNode()==this) return;
        textureInputs.add(input);
        
        countNodeDependancies();
        textureInputAdded(input);
    }
    
    protected void removeTextureInput(TextureInput input) {
        textureInputs.remove(input);
        countNodeDependancies();
    }
    
    private void countNodeDependancies() {
        Set<RenderNode> nodes = new HashSet<>();
        for (TextureInput t : textureInputs) nodes.add(t.getNode());
        nodeDependancyCount = nodes.size();
    }
    
    protected Set<RenderNode> getNodeDependancies() {
        Set<RenderNode> nodes = new HashSet<>();
        for (TextureInput t : textureInputs) nodes.add(t.getNode());
        return nodes;
    }
    
    public List<RenderNode> getNodeConnections() {
        List<RenderNode> connections = new ArrayList<>();
        
        for (TextureInput t : textureInputs) {
            connections.add(t.getNode());
        }
        return connections;
    }
    
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // ------ CONTROL HANDLING
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    
    public ControlTab createControlTab(String name) {
        ControlTab tab = new ControlTab(name);
        controlTabs.add(tab);
        return tab;
    }
    
    public ControlGroup createControlGroup(String name) {
        ControlTab tab = getLatestControlTab();
        if (tab==null) return null;
        
        ControlGroup group = new ControlGroup(name, tab);
        tab.add(group);
        return group;
    }
    
    public void addControlElement(ControlElement control, ControlGroup group) {
        if (group==null) return;
        group.add(control);
        
        registeredControls.add(control.config());
    }
    
    public ControlTab getLatestControlTab() {
        if (controlTabs.isEmpty()) return null;
        return controlTabs.get(controlTabs.size()-1);
    }
    
    public ControlGroup getLatestControlGroup() {
        ControlTab tab = getLatestControlTab();
        if (tab==null) return null;
        
        List<ControlGroup> tabGroups = tab.items();
        if (tabGroups.isEmpty()) return null;
        return tabGroups.get(tabGroups.size()-1);
    }
    
    public List<ControlTab> getControlTabs() {
        return List.copyOf(controlTabs);
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
        return addFloatControl(name, min, max, precision, increment, defaultValue, slider, getLatestControlGroup());
    }
    
    protected BindableValue<Float> addFloatControl(
        String name,
        float min,
        float max,
        int precision,
        float increment,
        float defaultValue,
        boolean slider,
        ControlGroup group
    ) {
        BindableValue<Float> binding = BindableValue.of(defaultValue);
        String key = group.tab().name()+"/"+group.name()+"/"+name;
        
        addControlElement(
            new ControlElement(
                new FloatControlConfig(
                    key,
                    name,
                    binding,
                    min,
                    max,
                    precision,
                    increment,
                    defaultValue,
                    slider
                ),
                group
            ),
            group
        );
        return binding;
    }
    
    protected BindableValue<Boolean> addBooleanControl(String name, boolean defaultValue) {
        BindableValue<Boolean> binding = BindableValue.of(defaultValue);
        ControlGroup group = getLatestControlGroup();
        String key = group.tab().name()+"/"+group.name()+"/"+name;
        
        addControlElement(
            new ControlElement(
                new BooleanControlConfig(
                    key,
                    name,
                    binding,
                    defaultValue
                ),
                group
            ),
            group
        );
        return binding;
    }
    
    protected void addActionControl(String name, String buttonText, Runnable action) {
        ControlGroup group = getLatestControlGroup();
        String key = group.tab().name()+"/"+group.name()+"/"+name;
        
        addControlElement(
            new ControlElement(
                new ActionControlConfig(
                    key,
                    "Resize to window",
                    "Run",
                    this::resizeToOutputWindow
                ),
                group
            ),
            group
        );
    }
    
    protected void setupControls() {
        createControlTab("Size");
        createControlGroup("Output Texture");
        
        widthControl = addFloatControl("Width", 1f, 10000, 0, 1f, 1500, false);
        heightControl = addFloatControl("Height", 1f, 10000, 0, 1f, 900, false);
        addActionControl("Resize to window", "Run", this::resizeToOutputWindow);
        
        widthControl.bind(this);
        heightControl.bind(this);
    }
    
    public void loadControlDefaults() {}
    
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // ------ OUTPUT RESIZING
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    
    public void resizeOutput(Vec2I dimensions) {
        this.outputWidth = dimensions.x;
        this.outputHeight = dimensions.y;
        
        if (renderTarget!=null) renderTarget.resize(dimensions);
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
    
    private void applyPendingResize() {
        if (pendingOutputWidth==null||pendingOutputHeight==null) return;
        
        resizeOutput(new Vec2I(pendingOutputWidth, pendingOutputHeight));
        
        pendingOutputWidth = null;
        pendingOutputHeight = null;
    }
    
    public void resizeToOutputWindow() {
        Vec2I d = SWMain.getOutputWindow().getFramebufferDimensions();
        pendingOutputWidth = d.x;
        pendingOutputHeight = d.y;
    }
    
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // ------ RENDERING
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    
    public final void update() {
        applyPendingResize();
    }
    
    public abstract void render();
    
    public void setRenderTarget(RenderTarget renderTarget) {
        this.renderTarget = renderTarget;
    }
    
    public RenderTarget getRenderTarget() {
        return renderTarget;
    }
    
    protected void uploadTextureInputs(ShaderManager sM) {
        List<TextureInput> textureInputs = getTextureInputs();
        
        for (int i=0; i<textureInputs.size(); i++) {
            TextureInput t = textureInputs.get(i);
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, t.getNode().getRenderTarget().getTexture());
            
            sM.setUniformInt("uInputTextures[" + i + "]", i);
            sM.setUniformInt("uInputModes[" + i + "]", t.getMode().ordinal());
        }
        sM.setUniformInt("uInputCount", textureInputs.size());
    }
    
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // ------ PERSISTANCE
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    
    public RenderNodeState getState() {
        // Control states
        List<RenderControlState> controlStates = new ArrayList<>();
        
        for (ControlConfig config : registeredControls) {
            if (!config.isStateful()) continue;
            RenderControlState state = config.getState();
            if (state!=null) controlStates.add(state);
        }
        
        // Input states
        List<TextureInputState> textureInputStates = new ArrayList<>();
        for (TextureInput t : textureInputs) textureInputStates.add(t.getState());
        
        return new RenderNodeState(
            id.toString(),
            longName,
            customName,
            (int) uiaPosition.x,
            (int) uiaPosition.y,
            controlStates,
            textureInputStates
        );
    }
    
    public static RenderNode loadFromState(RenderNodeState state) {  
        Class<? extends RenderNode> nodeClass = NODE_TYPE_REGISTRY.get(state.typeName());
        
        if (nodeClass==null) throw new RuntimeException("No RenderNode registry entry for: " + state.typeName());
        if (!RenderNode.class.isAssignableFrom(nodeClass)) throw new RuntimeException("Saved type is not a RenderNode: " + state.typeName());
        UUID id = UUID.fromString(state.id());
        RenderNode node = getNode(nodeClass, id);
        
        node.setCustomName(state.customName());
        node.setUIAPosition(new Vec2(state.uiaXPos(), state.uiaYPos()));
        
        return node;
    }
    
    public void applyControlStates(List<RenderControlState> states) {
        if (states == null) return;
        
        Map<String, RenderControlState> stateMap = new HashMap<>();
        
        for (RenderControlState state : states) {
            stateMap.put(state.key(), state);
        }
        
        for (ControlConfig config : registeredControls) {
            RenderControlState state = stateMap.get(config.key());
            if (state==null) continue;
            
            if (config instanceof FloatControlConfig c) {
                c.binding().setValue(state.value().getAsFloat());
            }
            else if (config instanceof BooleanControlConfig c) {
                c.binding().setValue(state.value().getAsBoolean());
            }
            else if (config instanceof IntegerControlConfig c) {
                c.binding().setValue(state.value().getAsInt());
            }
            else if (config instanceof StringControlConfig c) {
                c.binding().setValue(state.value().getAsString());
            }
        }
    }
    
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // ------ GETTERS & SETTERS
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    
    public int getNodeDependancyCount() {
        return nodeDependancyCount;
    }
    
    public int getMaxTextureInputs() {
        return maxTextureInputs;
    }
    
    public boolean allowsTextureInputs() {
        return maxTextureInputs>0;
    }
    
    protected List<TextureInput> getTextureInputs() {
        return textureInputs;
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
    
    public boolean hasSameID(String idString) {
        return idString.equals(id.toString());
    }
    
    public String getLongName() {
        return this.longName;
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
    
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // ------ CLEANUP
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    
    public void cleanup() {
        if (fullQuadVAO!=0) glDeleteVertexArrays(fullQuadVAO);
        if (fullQuadVBO!=0) glDeleteBuffers(fullQuadVBO);
        
        if (renderTarget!=null) renderTarget.cleanup();
        handleCleanup();
    }
    
    protected abstract void handleCleanup();
}
