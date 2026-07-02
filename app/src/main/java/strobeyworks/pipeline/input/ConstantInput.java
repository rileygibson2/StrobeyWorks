package strobeyworks.pipeline.input;

import strobeyworks.utils.BindableValue;

public abstract class ConstantInput<T> extends RenderInput {
    
    protected BindableValue<T> binding;
    
    public ConstantInput(BindableValue<T> binding) {
        super(false);
        this.binding = binding;
    }
    
    public boolean updateFrom(ConstantInput<?> other) {
        if (!getClass().equals(other.getClass())) return false;
        
        @SuppressWarnings("unchecked")
        ConstantInput<T> typedOther = (ConstantInput<T>) other;
        
        binding.setValue(typedOther.getBinding().getValue());
        return true;
    }
    
    public BindableValue<T> getBinding() {
        return binding;
    }
}
