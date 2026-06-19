package strobeyworks.nodes;

import java.util.ArrayList;
import java.util.List;

import strobeyworks.rendernodes.InspectorItem;

public abstract class RenderNode {
    
    private RenderTarget renderTarget;

    private int outputWidth;
    private int outputHeight;

    private List<InspectorItem> inspectorItems;

    public RenderNode() {
        this.outputWidth = -1;
        this.outputHeight = -1;

        inspectorItems = new ArrayList<>();
    }

    // Render

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

    // Controls/Inspectors

    public void addInspectorItem(InspectorItem item) {
        inspectorItems.add(item);
    }

    public List<InspectorItem> getInspectorItems() {
        return List.copyOf(inspectorItems);
    }

    protected abstract void setupControls();

    public void loadControlDefaults() {
        //for (ControlConfig<Float> c : floatControlConfigs) {
        //    c.binding().setValue(c.defaultValue());
        //}
    }

    // Output dimensions

    public void resizeOutput(int outputWidth, int outputHeight) {
        this.outputWidth = outputWidth;
        this.outputHeight = outputHeight;
        
        if (renderTarget!=null) renderTarget.resize(outputWidth, outputHeight);
        handleOutputResize();
    }

    protected abstract void handleOutputResize();

    public int getOutputWidth() {
        return outputWidth;
    }

    public int getOutputHeight() {
        return outputHeight;
    }

    // Cleanup

    public void cleanup() {
        if (renderTarget!=null) renderTarget.cleanup();
        handleCleanup();
    }

    protected abstract void handleCleanup();
}
