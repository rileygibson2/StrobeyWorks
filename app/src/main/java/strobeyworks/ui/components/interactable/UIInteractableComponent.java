package strobeyworks.ui.components.interactable;

import strobeyworks.ui.components.UIComponent;
import strobeyworks.ui.core.UIPair;
import strobeyworks.utils.Bindable;
import strobeyworks.utils.BindableObserver;

public abstract class UIInteractableComponent<B, L> extends UIComponent implements BindableObserver<B> {

    private UIInteractableAdaptor<B, L> adaptor;
    private Bindable<B> boundValue;
    private L localValue;
    
    public UIInteractableComponent(UIPair width, UIPair height, UIInteractableAdaptor<B, L> adaptor) {
        super(width, height);
        this.adaptor = adaptor;
    }
    
    public UIInteractableComponent(UIInteractableAdaptor<B, L> adaptor) {
        super();  
        this.adaptor = adaptor;
    }

    protected void setAdaptor(UIInteractableAdaptor<B, L> adaptor) {
        this.adaptor = adaptor;
    }
    
    protected abstract void implementLocalValueOnUI();
    
    protected abstract L getDefaultLocalValue();
    
    @Override
    public void initialise() {
        if (isBound()) setLocalValue(adaptor.adaptBoundToLocal(boundValue.getValue()));
        else if (!hasLocalValue()) setLocalValue(getDefaultLocalValue());
        else implementLocalValueOnUI();
    }
    
    public void commitLocalValue() {
        if (!isBound()) return;
        B newBoundValue = adaptor.adaptLocalToBound(localValue);
        if (newBoundValue!=null) boundValue.setValue(newBoundValue);
    }
    
    public void setLocalValue(L value) {
        localValue = value;
        implementLocalValueOnUI();
    }
    
    public L getLocalValue() {
        return localValue;
    }
    
    public B getBoundValue() {
        return isBound() ? boundValue.getValue() : null;
    }
    
    @Override
    public void bindableValueChanged(Bindable<B> v) {
        setLocalValue(adaptor.adaptBoundToLocal(v.getValue()));
    }
    
    public void bindTo(Bindable<B> value) {
        if (boundValue != null) boundValue.unbind(this);
        this.boundValue = value;
        boundValue.bind(this);
        
        if (isInitialised()) setLocalValue(adaptor.adaptBoundToLocal(value.getValue()));
    }
    
    public boolean isBound() {
        return this.boundValue!=null;
    }
    
    public boolean hasLocalValue() {
        return localValue!=null;
    }
}
