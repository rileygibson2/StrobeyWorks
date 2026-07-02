package strobeyworks.pipeline.controls;

import strobeyworks.pipeline.RenderNode;
import strobeyworks.pipeline.controls.ControlConfig.ActionControlConfig;
import strobeyworks.pipeline.controls.ControlConfig.DisplayControlConfig;
import strobeyworks.pipeline.input.RenderInputSlot;
import strobeyworks.utils.BindableValue;

public abstract class ControlElement extends ControlItem {
    
    private final RenderNode parentNode;
    private final ControlConfig<?> config;
    
    public ControlElement(RenderNode parentNode, ControlConfig<?> config) {
        super(config.name());
        this.config = config;
        this.parentNode = parentNode;
    }
    
    public RenderNode getParentNode() {
        return parentNode;
    }
    
    public ControlConfig<?> getConfig() {
        return this.config;
    }
    
    public static class InputControlElement extends ControlElement {
        private final RenderInputSlot slot;
        
        public InputControlElement(RenderNode parentNode, ControlConfig<?> config, RenderInputSlot slot) {
            super(parentNode, config);
            this.slot = slot;
        }
        
        public RenderInputSlot getRenderInputSlot() {
            return slot;
        }
    }
    
    public static class LocalControlElement<T> extends ControlElement {
        private final BindableValue<T> binding;
        
        public LocalControlElement(RenderNode parentNode, ControlConfig<T> config, BindableValue<T> binding) {
            super(parentNode, config);
            this.binding = binding;
        }
        
        public BindableValue<T> getBinding() {
            return binding;
        }
    }
    
    public static class ActionControlElement extends ControlElement {
        
        public ActionControlElement(RenderNode parentNode, ActionControlConfig config) {
            super(parentNode, config);
        }
    }
    
    public static class DisplayControlElement extends ControlElement {
        private final BindableValue<?> binding;
        
        public DisplayControlElement(RenderNode parentNode, DisplayControlConfig config, BindableValue<?> binding) {
            super(parentNode, config);
            this.binding = binding;
        }
        
        public BindableValue<?> getBinding() {
            return binding;
        }
    }
}
