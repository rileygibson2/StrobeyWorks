package strobeyworks.utils;

import java.util.HashSet;
import java.util.Set;

public class Bindable<T> {
    
    private T value;
    private Set<BindableObserver<T>> observers;

    public Bindable() {
        observers = new HashSet<>();
    }

    public Bindable(T value) {
        this.value = value;
        observers = new HashSet<>();
    }

    public void setValue(T value) {
        this.value = value;
        for (BindableObserver<T> o : observers) o.bindableValueChanged(this);
    }

    public T getValue() {
        return this.value;
    }

    public Bindable<T> bind(BindableObserver<T> observer) {
        observers.add(observer);
        return this;
    }

    public void unbind(BindableObserver<T> observer) {
        observers.remove(observer);
    }

    public static <T> Bindable<T> of(T value) {
        return new Bindable<>(value);
    }

    public static <T> Bindable<T> empty() {
        return new Bindable<>();
    }
}
