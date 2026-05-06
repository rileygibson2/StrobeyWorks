package strobeyworks.ui.components.interactable;

import strobeyworks.ui.components.UIComponent;
import strobeyworks.ui.core.UIPair;
import strobeyworks.utils.Bindable;
import strobeyworks.utils.BindableObserver;

/**
 * Interaction paths:
 * 
 * 
 */
public abstract class UIInteractableComponent<B, L> extends UIComponent implements BindableObserver<B> {
    
    private UIInteractableParser<B, L> parser;
    private Bindable<B> boundValue;
    private L localValue;
    
    public UIInteractableComponent(UIPair width, UIPair height, UIInteractableParser<B, L> parser) {
        super(width, height);
        this.parser = parser;
    }
    
    public UIInteractableComponent(UIInteractableParser<B, L> parser) {
        super();  
        this.parser = parser;
    }
    
    protected abstract void implementLocalValueOnUI();

    protected abstract L getDefaultLocalValue();

    @Override
    public void initialise() {
        if (!isBound()&&!hasLocalValue()) setLocalValue(getDefaultLocalValue());
        implementLocalValueOnUI();
    }
    
    public void commitLocalValue() {
        if (!isBound()) return;
        B newBoundValue = parser.parseLocalToBound(localValue);
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
        setLocalValue(parser.parseBoundToLocal(v.getValue()));
    }
    
    public void bindTo(Bindable<B> bindable) {
        if (boundValue != null) boundValue.unbind(this);
        this.boundValue = bindable;
        boundValue.bind(this);
        
        if (isInitialised()) implementLocalValueOnUI();
    }
    
    public boolean isBound() {
        return this.boundValue!=null;
    }

    public boolean hasLocalValue() {
        return localValue!=null;
    }
}
