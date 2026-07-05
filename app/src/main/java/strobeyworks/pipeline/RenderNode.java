package strobeyworks.pipeline;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonElement;

import strobeyworks.SWMain;
import strobeyworks.pipeline.controls.ControlConfig.ActionControlConfig;
import strobeyworks.pipeline.controls.ControlConfig.BooleanControlConfig;
import strobeyworks.pipeline.controls.ControlConfig.DisplayControlConfig;
import strobeyworks.pipeline.controls.ControlConfig.FloatControlConfig;
import strobeyworks.pipeline.controls.ControlConfig.SelectControlConfig;
import strobeyworks.pipeline.controls.ControlElement.ActionControlElement;
import strobeyworks.pipeline.controls.ControlElement.DisplayControlElement;
import strobeyworks.pipeline.controls.ControlElement.InputControlElement;
import strobeyworks.pipeline.controls.ControlElement.LocalControlElement;
import strobeyworks.pipeline.controls.ControlItem.ControlGroup;
import strobeyworks.pipeline.controls.ControlItem.ControlTab;
import strobeyworks.pipeline.input.BooleanConstantInput;
import strobeyworks.pipeline.input.FloatConstantInput;
import strobeyworks.pipeline.input.RenderInput;
import strobeyworks.pipeline.input.RenderInputSlot;
import strobeyworks.pipeline.input.RenderInputSlot.RenderInputSlotState;
import strobeyworks.pipeline.input.SelectConstantInput;
import strobeyworks.pipeline.input.TextureInput;
import strobeyworks.platform.ShaderManager;
import strobeyworks.rendernodes.AgentNode;
import strobeyworks.rendernodes.MaskNode;
import strobeyworks.rendernodes.MixNode;
import strobeyworks.rendernodes.PerlinNode;
import strobeyworks.rendernodes.PixelDots;
import strobeyworks.utils.BindableValue;
import strobeyworks.utils.BindableValueObserver;
import strobeyworks.utils.Vec2I;

public abstract class RenderNode implements BindableValueObserver<Float> {
    
    public static final Map<String, Class<? extends RenderNode>> NODE_TYPE_REGISTRY = Map.of(
        "Perlin-Noise", PerlinNode.class,
        "Species-Agents", AgentNode.class,
        "Mix", MixNode.class,
        "Mask", MaskNode.class,
        "Pixel-Dots", PixelDots.class
    );
    
    public record RenderNodeState(
        String id,
        String typeName,
        String customName,
        int uiaXPos,
        int uiaYPos,
        List<RenderInputSlotState> feedSlots,
        List<RenderInputSlotState> parameterSlots
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
    
    private List<RenderInputSlot> feedSlots;
    private List<RenderInputSlot> parameterSlots;
    
    private final boolean hasTextureOutput;
    
    private int outputWidth;
    private int outputHeight;
    
    private List<ControlTab> controlTabs;
    
    private BindableValue<Float> widthControl;
    private BindableValue<Float> heightControl;
    
    private BindableValue<Integer> uiaPosX;
    private BindableValue<Integer> uiaPosY;
    
    private boolean syncingOutputSizeControls;
    private Integer pendingOutputWidth;
    private Integer pendingOutputHeight;
    
    public RenderNode(UUID id, String longName, String shortName, boolean hasTextureOutput) {
        this.id = id;
        this.longName = longName;
        this.shortName = shortName;
        this.customName = "";
        
        feedSlots = new ArrayList<>();
        parameterSlots = new ArrayList<>();
        
        this.hasTextureOutput = hasTextureOutput;
        
        this.outputWidth = -1;
        this.outputHeight = -1;
        
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
    
    //protected abstract void textureInputAdded(TextureInput input);
    
    protected RenderInputSlot createFeedSlot(String uniformName, boolean allowsTexture) {
        RenderInputSlot slot = new RenderInputSlot("feed"+feedSlots.size()+1, uniformName, allowsTexture);
        feedSlots.add(slot);
        return slot;
    }
    
    protected RenderInputSlot createParameterSlot(String id, String uniformName, boolean allowsTexture) {
        RenderInputSlot slot = new RenderInputSlot(id, uniformName, allowsTexture);
        parameterSlots.add(slot);
        return slot;
    }
    
    public boolean setFeedInput(int feedInput, RenderInput input) {
        if (feedInput<0||feedInput>feedSlots.size()-1) return false;
        
        RenderInputSlot slot = feedSlots.get(feedInput);
        if (slot==null) return false;
        
        if (setSlotInput(slot, input)) {
            feedInputsChanged();
            return true;
        }
        
        return false;
    }
    
    public abstract void feedInputsChanged();
    
    public boolean setSlotInput(RenderInputSlot slot, RenderInput input) {
        // Validation
        if (!slot.accepts(input)) return false;
        
        if (input instanceof TextureInput texInput) {
            if (texInput.getSourceNode()==this) return false;
        }
        
        // Try setting new input
        RenderInput oldInput = slot.getInput();
        slot.setInput(input);
        
        if (!validateNode()) {
            slot.setInput(oldInput);
            return false;
        }
        
        RenderPipeline.getInstance().handleNodeInputsChanged(this);
        return true;
    }
    
    private boolean validateNode() {
        if (!RenderPipeline.getInstance().tryCompile()) return false;
        return true;
    }
    
    public List<RenderNode> getDependancies() {
        List<RenderNode> nodes = new ArrayList<>();
        
        for (RenderInputSlot slot : feedSlots) {
            if (!slot.hasInput()) continue;
            RenderInput i = slot.getInput();
            if (i instanceof TextureInput t) nodes.add(t.getSourceNode());
        }
        
        for (RenderInputSlot slot : parameterSlots) {
            if (!slot.hasInput()) continue;
            RenderInput i = slot.getInput();
            if (i instanceof TextureInput t) nodes.add(t.getSourceNode());
        }
        
        return nodes;
    }

    public RenderInputSlot getSlotById(String id) {
        for (RenderInputSlot slot : feedSlots) {
            if (slot.getId().equals(id)) return slot;
        }
        
        for (RenderInputSlot slot : parameterSlots) {
            if (slot.getId().equals(id)) return slot;
        }
        
        return null;
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
    
    public List<ControlTab> getControlTabs() {
        return List.copyOf(controlTabs);
    }
    
    public ControlGroup createControlGroup(String name, ControlTab tab) {
        if (tab==null) return null;
        
        ControlGroup group = new ControlGroup(name);
        tab.add(group);
        return group;
    }
    
    protected RenderInputSlot floatParam(
        ControlGroup group,
        String label,
        String uniformName,
        boolean allowsTexture,
        float min,
        float max,
        int precision,
        float increment,
        float defaultValue,
        boolean slider
    ) {
        String id = label.toLowerCase().replaceAll(" ", "_");
        RenderInputSlot slot = createParameterSlot(id, uniformName, allowsTexture);
        slot.setInput(new FloatConstantInput(defaultValue));
        
        FloatControlConfig config = new FloatControlConfig(
            label,
            min,
            max,
            precision,
            increment,
            defaultValue,
            slider
        );
        
        group.add(new InputControlElement(this, config, slot));
        return slot;
    }
    
    protected RenderInputSlot boolParam(
        ControlGroup group,
        String label,
        String uniformName,
        boolean defaultValue
    ) {
        String id = label.toLowerCase().replaceAll(" ", "_");
        RenderInputSlot slot = createParameterSlot(id, uniformName, false);
        slot.setInput(new BooleanConstantInput(defaultValue));
        
        group.add(new InputControlElement(
            this,
            new BooleanControlConfig(label, defaultValue),
            slot
        ));
        
        return slot;
    }
    
    protected RenderInputSlot selectParam(
        ControlGroup group,
        String label,
        String uniformName,
        String[] options,
        int defaultValue
    ) {
        String id = label.toLowerCase().replaceAll(" ", "_");
        RenderInputSlot slot = createParameterSlot(id, uniformName, false);
        slot.setInput(new SelectConstantInput(options, defaultValue));
        
        group.add(new InputControlElement(
            this,
            new SelectControlConfig(label, options, defaultValue),
            slot
        ));
        
        return slot;
    }
    
    protected BindableValue<Float> floatLocal(
        ControlGroup group,
        String label,
        float min,
        float max,
        int precision,
        float increment,
        float defaultValue,
        boolean slider
    ) {
        BindableValue<Float> binding = BindableValue.of(defaultValue);
        
        group.add(new LocalControlElement<Float>(
            this,
            new FloatControlConfig(label, min, max, precision, increment, defaultValue, slider),
            binding
        ));
        
        return binding;
    }
    
    protected void actionLocal(
        ControlGroup group,
        String label,
        String buttonText,
        Runnable action
    ) {
        ActionControlConfig c = new ActionControlConfig(
            label,
            buttonText,
            action
        );
        
        group.add(new ActionControlElement(this, c));
    }
    
    protected void displayLocal(
        ControlGroup group,
        String label,
        BindableValue<?> binding
    ) {
        DisplayControlConfig c = new DisplayControlConfig(label);
        
        group.add(new DisplayControlElement(this, c, binding));
    }
    
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // ------ SETUP
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    
    protected void setup() {
        setupFeeds();
        setupParameters();
        
        ControlTab t = createControlTab("Size");
        ControlGroup g = createControlGroup("Output Texture", t);
        
        widthControl = floatLocal(g, "Width", 1f, 10000, 0, 1f, 1500, false);
        heightControl = floatLocal(g, "Height", 1f, 10000, 0, 1f, 900, false);
        actionLocal(g, "Resize to window", "Run", this::resizeToOutputWindow);
        
        g = createControlGroup("Display", t);
        
        uiaPosX = BindableValue.of(0);
        uiaPosY = BindableValue.of(0);
        
        displayLocal(g, "X", uiaPosX);
        displayLocal(g, "Y", uiaPosY);
        
        
        widthControl.bind(this);
        heightControl.bind(this);
    }
    
    protected abstract void setupFeeds();
    
    protected abstract void setupParameters();
    
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
    
    public void uploadInputs(ShaderManager sM) {
        // Upload static inputs
        sM.setUniformInt("uOutputWidth", getOutputWidth());
        sM.setUniformInt("uOutputHeight", getOutputHeight());
        sM.setUniformFloat("uOutputAspect", getOutputAspectRatio());
        
        // Upload feed and parameter inputs
        int currentTextureUnit = 0;
        
        for (RenderInputSlot slot : feedSlots) {
            currentTextureUnit = slot.upload(sM, currentTextureUnit);
        }
        
        for (RenderInputSlot slot : parameterSlots) {
            currentTextureUnit = slot.upload(sM, currentTextureUnit);
        }
    }
    
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // ------ PERSISTANCE
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    
    public RenderNodeState getState() {
        List<RenderInputSlotState> feedSlotStates = new ArrayList<>();
        for (RenderInputSlot slot : feedSlots) {
            feedSlotStates.add(slot.getState());
        }

        List<RenderInputSlotState> parameterSlotStates = new ArrayList<>();
        for (RenderInputSlot slot : parameterSlots) {
            parameterSlotStates.add(slot.getState());
        }
        
        return new RenderNodeState(
            id.toString(),
            longName,
            customName,
            (int) uiaPosX.getValue(),
            (int) uiaPosY.getValue(),
            feedSlotStates,
            parameterSlotStates
        );
    }
    
    /*public static RenderNode loadFromState(RenderNodeState state) {  
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
    }*/
    
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // ------ GETTERS & SETTERS
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    
    public boolean hasTextureOutput() {
        return hasTextureOutput;
    }
    
    public int getFeedSlotCount() {
        return feedSlots.size();
    }
    
    public boolean allowsFeedInputs() {
        return feedSlots.size()>0;
    }
    
    public void setCustomName(String name) {
        this.customName = name;
    }
    
    public void setUIAPosition(int x, int y) {
        this.uiaPosX.setValue(x);
        this.uiaPosY.setValue(y);
    }
    
    public int getOutputWidth() {
        return outputWidth;
    }
    
    public int getOutputHeight() {
        return outputHeight;
    }
    
    public float getOutputAspectRatio() {
        if (outputHeight==0) return 1f;
        return (float) outputWidth / (float) outputHeight;
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
    
    public int getUIAPosX() {
        return uiaPosX.getValue();
    }
    
    public int getUIAPosY() {
        return uiaPosY.getValue();
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
