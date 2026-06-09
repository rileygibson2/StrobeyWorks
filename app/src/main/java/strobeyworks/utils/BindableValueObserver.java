package strobeyworks.utils;

public interface BindableValueObserver<T> {
    
    public void bindableValueChanged(BindableValue<T> v);
}
