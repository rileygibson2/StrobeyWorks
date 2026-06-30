package strobeyworks.pipeline.controls;

import strobeyworks.pipeline.RenderNode;
import strobeyworks.pipeline.input.RenderInputSlot;
import strobeyworks.utils.BindableValue;

public class ControlElement extends ControlItem {
    
    private final RenderNode parentNode;
    private final ControlConfig config;
    private final RenderInputSlot slot;
    private final BindableValue<?> binding;

    public ControlElement(RenderNode parentNode, ControlConfig config, RenderInputSlot slot) {
        super(config.name());
        this.config = config;
        this.slot = slot;
        this.parentNode = parentNode;

        this.binding = null;
    }

    public ControlElement(RenderNode parentNode, ControlConfig config, BindableValue<?> binding) {
        super(config.name());
        this.config = config;
        this.binding = binding;
        this.parentNode = parentNode;

        this.slot = null;
    }

    public ControlElement(RenderNode parentNode, ControlConfig config) {
        super(config.name());
        this.config = config;
        this.parentNode = parentNode;

        this.slot = null;
        this.binding = null;
    }

    public RenderNode getParentNode() {
        return parentNode;
    }

    public ControlConfig getConfig() {
        return this.config;
    }

    public RenderInputSlot getRenderInputSlot() {
        return slot;
    }

    public boolean hasRenderInputSlot() {
        return slot!=null;
    }

    public BindableValue<?> getBinding() {
        return binding;
    }

    public boolean hasBinding() {
        return binding!=null;
    }
}
