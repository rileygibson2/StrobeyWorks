package strobeyworks.utils;

public interface BindableObserver<T> {
    
    public void bindableValueChanged(Bindable<T> v);
}
