package strobeyworks.ui.components.input;

import strobeyworks.ui.core.UILength;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.utils.Bindable;
import strobeyworks.utils.BindableObserver;

/**
* Defines this element as having a local value and optionally a binding to an external value.
* The external value is represented here as a Bindable object.
* This external value may have a different type to the local value and so the class is generic for two types;
* E represents the external value type, L represents the local value type.
* 
* Under this system, subclasses may define UI behaviour surronding a local value and then, at some point, attempt
* to commit that local value to an external value if one is bound.
* This commitment process invokes the use of a UIValueAdaptor instance which provides somme value validation and
* remapping as well as handles the actual conversion from L to E.
* 
* When an external value is updated, this UIValueAdaptor also handles the conversion from the external type E to the
* local type L.
* 
* Alternativly subclasses may not use an externally bound value at all and in that case this class provides the same
* validation and remapping for a locally committed value.
*/
public abstract class UIValueControl<E, L> extends UIRectangle implements BindableObserver<E> {
    
    private UIValueAdaptor<E, L> adaptor;
    private Bindable<E> binding;
    private L localValue;
    
    public UIValueControl(UILength width, UILength height, UIValueAdaptor<E, L> adaptor) {
        super(width, height);
        this.adaptor = adaptor;
    }
    
    public UIValueControl(UIValueAdaptor<E, L> adaptor) {
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

    public boolean commitValue(E value) {
        L local = adaptor.adaptExternalToLocal(value);
        setLocalValue(local);
        return commitLocalValue();
    }
    
    public L getLocalValue() {
        return localValue;
    }
    
    protected E previewExternalValue() {
        return adaptor.adaptLocalToExternal(localValue);
    }
    
    public void setLocalValue(L value) {
        localValue = value;
        implementLocalValueOnUI();
    }
    
    public E getLocalValueAsExternal() {
        return adaptor.adaptLocalToExternal(localValue);
    }
    
    @Override
    public void bindableValueChanged(Bindable<E> v) {
        setLocalValue(adaptor.adaptExternalToLocal(v.getValue()));
    }
    
    public void bindTo(Bindable<E> binding) {
        if (this.binding!=null) this.binding.unbind(this);
        
        this.binding = binding;
        
        if (this.binding!=null) {
            this.binding.bind(this);
            if (isInitialised()) setLocalValue(adaptor.adaptExternalToLocal(this.binding.getValue()));
        }
    }
    
    public boolean hasBinding() {
        return this.binding!=null;
    }
    
    public boolean hasLocalValue() {
        return localValue!=null;
    }
}
