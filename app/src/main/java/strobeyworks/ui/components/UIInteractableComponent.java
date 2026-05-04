package strobeyworks.ui.components;

import strobeyworks.ui.primitives.UIPair;

public abstract class UIInteractableComponent<T> extends UIComponent {
    
    protected T value;

    @FunctionalInterface
    public interface UIInteractableCallback<T> {
        void implement(T value);
    }

    private UIInteractableCallback<T> valueChangedCallback;

    public UIInteractableComponent(UIPair width, UIPair height) {
        super(width, height);
    }

    public UIInteractableComponent() {
        super();
    }

    public abstract void valueUpdated();

    public void userSetValue(T value) {
        this.value = value;
        valueUpdated();
        if (valueChangedCallback!=null) valueChangedCallback.implement(value);
    }

    public void setValue(T value) {
        this.value = value;
        valueUpdated();
    }

    public void setValueChangedCallback(UIInteractableCallback<T> valueChangedCallback) {
        this.valueChangedCallback = valueChangedCallback;
    }
}
