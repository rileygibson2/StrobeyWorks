package strobeyworks.ui.components.interactable;

import strobeyworks.ui.components.UIComponent;
import strobeyworks.ui.core.UIPair;
import strobeyworks.utils.Bindable;
import strobeyworks.utils.BindableObserver;

public abstract class UIInteractableComponent<E, L> extends UIComponent implements BindableObserver<E> {

    private UIValueAdaptor<E, L> adaptor;
    private Bindable<E> binding;
    private L localValue;
    
    public UIInteractableComponent(UIPair width, UIPair height, UIValueAdaptor<E, L> adaptor) {
        super(width, height);
        this.adaptor = adaptor;
    }
    
    public UIInteractableComponent(UIValueAdaptor<E, L> adaptor) {
        super();  
        this.adaptor = adaptor;
    }
    
    protected abstract void implementLocalValueOnUI();
    
    protected abstract L getDefaultLocalValue();
    
    @Override
    public void initialise() {
        if (hasBinding()) setLocalValue(adaptor.adaptExternalToLocal(binding.getValue()));
        else if (!hasLocalValue()) setLocalValue(getDefaultLocalValue());
        else implementLocalValueOnUI();
    }
    
    public boolean commitLocalValue() {
        E validated = adaptor.adaptLocalToExternal(localValue);
        if (validated==null) return false;

        if (hasBinding()) binding.setValue(validated); // This will set local value again on call back
        else setLocalValue(adaptor.adaptExternalToLocal(validated));
        return true;
    }
    
    public void setLocalValue(L value) {
        localValue = value;
        implementLocalValueOnUI();
    }
    
    public L getLocalValue() {
        return localValue;
    }
    
    @Override
    public void bindableValueChanged(Bindable<E> v) {
        setLocalValue(adaptor.adaptExternalToLocal(v.getValue()));
    }
    
    public void bindTo(Bindable<E> binding) {
        if (binding!=null) binding.unbind(this);
        this.binding = binding;
        binding.bind(this);
        
        if (isInitialised()) setLocalValue(adaptor.adaptExternalToLocal(binding.getValue()));
    }
    
    public boolean hasBinding() {
        return this.binding!=null;
    }
    
    public boolean hasLocalValue() {
        return localValue!=null;
    }
}
