package strobeyworks.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import strobeyworks.rendernodes.InspectorItem;
import strobeyworks.rendernodes.InspectorItem.InspectorControl;
import strobeyworks.rendernodes.InspectorItem.InspectorGroup;
import strobeyworks.rendernodes.configs.BooleanControlConfig;
import strobeyworks.rendernodes.configs.FloatControlConfig;
import strobeyworks.utils.BindableValue;
import strobeyworks.utils.Vec2;

public abstract class RenderNode {
    
    private final String id;
    private String nodeTypeName;
    private String customName;
    private RenderTarget renderTarget;

    private int outputWidth;
    private int outputHeight;

    private List<InspectorItem> inspectorItems;

    private Vec2 uiaPosition;

    public RenderNode(String nodeTypeName) {
        this.id = UUID.randomUUID().toString();
        this.nodeTypeName = nodeTypeName;
        this.customName = "";

        this.outputWidth = -1;
        this.outputHeight = -1;

        this.uiaPosition = new Vec2(0f);

        inspectorItems = new ArrayList<>();

        setupControls();
    }

    public void initialise(int outputWidth, int outputHeight) {
        this.outputWidth = outputWidth;
        this.outputHeight = outputHeight;
    }

    public abstract void update();
    
    public abstract void render();

    public void setRenderTarget(RenderTarget renderTarget) {
        this.renderTarget = renderTarget;
    }

    public RenderTarget getRenderTarget() {
        return renderTarget;
    }

    public void addInspectorItem(InspectorItem item) {
        inspectorItems.add(item);
    }

    public List<InspectorItem> getInspectorItems() {
        return List.copyOf(inspectorItems);
    }

    protected BindableValue<Float> addFloatControl(
        InspectorGroup g,
        String name,
        float min,
        float max,
        int precision,
        float increment,
        float defaultValue
    ) {
        BindableValue<Float> binding = BindableValue.of(defaultValue);
        g.items().add(new InspectorControl(new FloatControlConfig(name, binding, min, max, precision, increment, defaultValue)));
        return binding;
    }
    
    protected BindableValue<Boolean> addBooleanControl(
        InspectorGroup g,
        String name,
        boolean defaultValue
    ) {
        BindableValue<Boolean> binding = BindableValue.of(defaultValue);
        g.items().add(new InspectorControl(new BooleanControlConfig(name, binding, defaultValue)));
        return binding;
    }

    protected abstract void setupControls();

    public void loadControlDefaults() {
        //for (ControlConfig<Float> c : floatControlConfigs) {
        //    c.binding().setValue(c.defaultValue());
        //}
    }

    public void resizeOutput(int outputWidth, int outputHeight) {
        this.outputWidth = outputWidth;
        this.outputHeight = outputHeight;
        
        if (renderTarget!=null) renderTarget.resize(outputWidth, outputHeight);
        handleOutputResize();
    }

    protected abstract void handleOutputResize();

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

    public String getNodeTypeName() {
        return this.nodeTypeName;
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
