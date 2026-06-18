package strobeyworks.ui.components.input;

import strobeyworks.logger.Logger;
import strobeyworks.ui.components.input.UIValueMapper.UIMapResult;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.utils.BindableValue;
import strobeyworks.utils.BindableValueObserver;

/**
* Defines this element as having a local value and optionally a binding to an external value.
* The external value is represented here as a Bindable object.
* This external value may have a different type to the local value and so the class is generic for two types;
* E represents the external value type, L represents the local value type.
* 
* Under this system, subclasses may define UI behaviour surronding a local value and then, at some point, attempt
* to commit that local value to an external value if one is bound.
* This commitment process invokes the use of a UIValuemapper instance which provides somme value validation and
* remapping as well as handles the actual conversion from L to E.
* 
* When an external value is updated, this UIValuemapper also handles the conversion from the external type E to the
* local type L.
* 
* Alternativly subclasses may not use an externally bound value at all and in that case this class provides the same
* validation and remapping for a locally committed value.
*/
public abstract class UIBindableInput<E, L> extends UIRectangle implements BindableValueObserver<E> {
    
    private UIValueMapper<E, L> mapper;
    private BindableValue<E> binding;
    private L localValue;
    
    public UIBindableInput(UIValueMapper<E, L> mapper) {
        super();  
        this.mapper = mapper;
    }
    
    protected abstract void implementLocalValueOnUI();
    
    protected abstract L getDefaultLocalValue();
    
    @Override
    public void initialise() {
        super.initialise();
        
        if (hasBinding()) {
            UIMapResult<L> result = mapper.mapExternalToLocal(binding.getValue());
            if (result.success()) setLocalValue(result.value());
            setLocalValue(result.value());
        }
        else if (!hasLocalValue()) setLocalValue(getDefaultLocalValue());
        else implementLocalValueOnUI();
    }
    
    public boolean commitLocalValue() {
        UIMapResult<E> result = mapper.mapLocalToExternal(localValue);
        if (!result.success()) return false;
        
        if (hasBinding()) binding.setValue(result.value()); // This will set local value again on call back
        else setLocalValue(mapper.mapExternalToLocal(result.value()).value());
        return true;
    }
    
    public void commitValue(E value) {
        UIMapResult<L> result = mapper.mapExternalToLocal(value);
        if (!result.success()) return;
        
        setLocalValue(result.value());
        commitLocalValue();
    }
    
    public L getLocalValue() {
        return localValue;
    }
    
    protected E previewExternalValue() {
        return mapper.mapLocalToExternal(localValue).value();
    }
    
    public void setLocalValue(L value) {
        localValue = value;
        implementLocalValueOnUI();
    }
    
    public E getLocalValueAsExternal() {
        return mapper.mapLocalToExternal(localValue).value();
    }
    
    @Override
    public void bindableValueChanged(BindableValue<E> v) {
        UIMapResult<L> result = mapper.mapExternalToLocal(v.getValue());
        if (!result.success()) return;
        
        setLocalValue(result.value());
    }
    
    public void bindTo(BindableValue<E> binding) {
        if (this.binding!=null) this.binding.unbind(this);
        
        this.binding = binding;
        
        if (this.binding!=null) {
            this.binding.bind(this);
            if (isInitialised()) bindableValueChanged(binding);;
        }
    }
    
    public boolean hasBinding() {
        return this.binding!=null;
    }
    
    public boolean hasLocalValue() {
        return localValue!=null;
    }
}
