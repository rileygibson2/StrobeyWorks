package strobeyworks.ui.components.interactable;

public interface UIInteractableAdaptor<B, L> {

    public static final UIInteractableAdaptor<Float, Float> FLOAT_IDENTITY = new UIInteractableAdaptor<>() {
        public Float adaptBoundToLocal(Float value) {return value;}
        public Float adaptLocalToBound(Float value) {return value;}
    };

    public static final UIInteractableAdaptor<Boolean, Boolean> BOOLEAN_IDENTITY = new UIInteractableAdaptor<>() {
        public Boolean adaptBoundToLocal(Boolean value) {return value;}
        public Boolean adaptLocalToBound(Boolean value) {return value;}
    };

    public L adaptBoundToLocal(B boundValue);
    public B adaptLocalToBound(L localValue);
}
