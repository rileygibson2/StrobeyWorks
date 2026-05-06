package strobeyworks.ui.components.interactable;

public interface UIValueAdaptor<E, L> {

    public static final UIValueAdaptor<Float, Float> FLOAT_IDENTITY = new UIValueAdaptor<>() {
        public Float adaptExternalToLocal(Float value) {return value;}
        public Float adaptLocalToExternal(Float value) {return value;}
    };

    public static final UIValueAdaptor<Boolean, Boolean> BOOLEAN_IDENTITY = new UIValueAdaptor<>() {
        public Boolean adaptExternalToLocal(Boolean value) {return value;}
        public Boolean adaptLocalToExternal(Boolean value) {return value;}
    };

    public L adaptExternalToLocal(E externalValue);
    public E adaptLocalToExternal(L localValue);
}
