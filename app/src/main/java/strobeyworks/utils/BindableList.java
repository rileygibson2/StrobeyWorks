package strobeyworks.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BindableList<T> {
    
    private List<T> values;
    private Set<BindableListObserver> observers;
    
    public BindableList() {
        values = new ArrayList<>();
        observers = new HashSet<>();
    }
    
    public void set(T value) {
        values.add(value);
        for (BindableListObserver o : observers) o.bindableListChanged(this);
    }

    public void remove(T value) {
        values.remove(value);
        for (BindableListObserver o : observers) o.bindableListChanged(this);
    }
    
    public T get(int i) {
        return values.get(i);
    }
    
    public List<T> getValues() {
        return Collections.unmodifiableList(values);
    }

    public int size() {
        return values.size();
    }

    public boolean isEmpty() {
        return values.size()==0;
    }
    
    public BindableList<T> bind(BindableListObserver observer) {
        observers.add(observer);
        return this;
    }
    
    public void unbind(BindableListObserver observer) {
        observers.remove(observer);
    }
}
