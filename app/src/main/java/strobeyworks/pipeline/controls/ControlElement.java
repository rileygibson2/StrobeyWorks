package strobeyworks.pipeline.controls;

import strobeyworks.pipeline.RenderNode;
import strobeyworks.pipeline.input.RenderInput;
import strobeyworks.utils.BindableValue;

public class ControlElement extends ControlItem {
    
    private RenderNode parentNode;
    
    private ControlGroup parent;
    private ControlConfig config;

    private RenderInput input;
    private BindableValue<?> binding;

    public ControlElement(RenderNode parentNode, RenderInput input, ControlConfig config) {
        super(config.name());
        this.config = config;
        this.input = input;
        this.parentNode = parentNode;
    }

    public ControlElement(RenderNode parentNode, BindableValue<?> binding, ControlConfig config) {
        super(config.name());
        this.binding = binding;
        this.parentNode = parentNode;
    }

    public ControlElement(RenderNode parentNode, ControlConfig config) {
        super(config.name());
        this.config = config;
        this.parentNode = parentNode;
    }

    public void setParent(ControlGroup group) {
        this.parent = group;
    }

    public ControlGroup getParent() {
        return this.parent;
    }

    public RenderNode getParentNode() {
        return parentNode;
    }

    public ControlConfig getConfig() {
        return this.config;
    }

    public void setRenderInput(RenderInput input) {
        this.input = input;
    }

    public RenderInput getRenderInput() {
        return this.input;
    }

    public BindableValue<?> getBinding() {
        return binding;
    }
}
