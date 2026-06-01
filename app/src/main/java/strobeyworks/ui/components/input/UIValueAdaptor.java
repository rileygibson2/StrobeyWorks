package strobeyworks.ui.components.input;

import strobeyworks.utils.Utils;

public interface UIValueAdaptor<E, L> {

    public static final UIValueAdaptor<Float, Float> FLOAT_IDENTITY = new UIValueAdaptor<>() {
        public Float adaptExternalToLocal(Float value) {return value;}
        public Float adaptLocalToExternal(Float value) {return value;}
    };

    public static final UIValueAdaptor<Boolean, Boolean> BOOLEAN_IDENTITY = new UIValueAdaptor<>() {
        public Boolean adaptExternalToLocal(Boolean value) {return value;}
        public Boolean adaptLocalToExternal(Boolean value) {return value;}
    };

    public static UIValueAdaptor<Float, Float> floatRange(float min, float max) {
        return new UIValueAdaptor<>() {
            public Float adaptExternalToLocal(Float externalValue) {
                if (externalValue==null) return null;
                return Utils.clamp01((externalValue-min)/(max-min));
            }

            public Float adaptLocalToExternal(Float localValue) {
                if (localValue==null) return null;
                return Utils.lerpFloat(min, max, Utils.clamp01(localValue));
            }
        };
    }

    public L adaptExternalToLocal(E externalValue);
    public E adaptLocalToExternal(L localValue);
}
