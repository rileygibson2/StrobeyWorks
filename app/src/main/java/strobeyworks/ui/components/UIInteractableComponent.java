package strobeyworks.ui.components;

import strobeyworks.ui.core.UIPair;
import strobeyworks.utils.Bindable;
import strobeyworks.utils.BindableObserver;

public abstract class UIInteractableComponent<T> extends UIComponent implements BindableObserver<T> {
    
    private Bindable<T> boundValue;
    private T localValue;
    
    public UIInteractableComponent(UIPair width, UIPair height) {
        super(width, height);
    }
    
    public UIInteractableComponent() {
        super();
    }
    
    protected abstract void implementValue();
    
    public void setValue(T value) {
        if (isBound()) boundValue.setValue(value);
        else {
            localValue = value;
            implementValue();
        }
    }
    
    public T getValue() {
        if (isBound()) return boundValue.getValue();
        return localValue;
    }
    
    @Override
    public void bindableValueChanged(Bindable<T> v) {
        implementValue();
    }
    
    public void bindTo(Bindable<T> bindable) {
        if (boundValue != null) boundValue.unbind(this);
        this.boundValue = bindable;
        boundValue.bind(this);
        
        if (isInitialised()) implementValue();
    }
    
    public boolean isBound() {
        return this.boundValue!=null;
    }
}
