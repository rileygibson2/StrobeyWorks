package strobeyworks.utils;

import java.util.HashSet;
import java.util.Set;

public class BindableValue<T> {
    
    private T value;
    private Set<BindableValueObserver<T>> observers;

    public BindableValue() {
        observers = new HashSet<>();
    }

    public BindableValue(T value) {
        this.value = value;
        observers = new HashSet<>();
    }

    public void setValue(T value) {
        this.value = value;
        for (BindableValueObserver<T> o : observers) o.bindableValueChanged(this);
    }

    public T getValue() {
        return this.value;
    }

    public BindableValue<T> bind(BindableValueObserver<T> observer) {
        observers.add(observer);
        return this;
    }

    public void unbind(BindableValueObserver<T> observer) {
        observers.remove(observer);
    }

    public static <T> BindableValue<T> of(T value) {
        return new BindableValue<>(value);
    }

    public static <T> BindableValue<T> empty() {
        return new BindableValue<>();
    }
}
