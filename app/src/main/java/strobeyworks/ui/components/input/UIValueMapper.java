package strobeyworks.ui.components.input;

import strobeyworks.utils.Utils;

public interface UIValueMapper<E, L> {
    
    public record UIMapResult<T>(boolean success, T value) {
        
        public static <T> UIMapResult<T> success(T value) {
            return new UIMapResult<>(true, value);
        }
        
        public static <T> UIMapResult<T> failure() {
            return new UIMapResult<>(false, null);
        }
    }
    
    public static final UIValueMapper<Float, Float> FLOAT_IDENTITY = new UIValueMapper<>() {
        public UIMapResult<Float> mapExternalToLocal(Float value) {return UIMapResult.success(value);}
        public UIMapResult<Float> mapLocalToExternal(Float value) {return UIMapResult.success(value);}
    };
    
    public static final UIValueMapper<Boolean, Boolean> BOOLEAN_IDENTITY = new UIValueMapper<>() {
        public UIMapResult<Boolean> mapExternalToLocal(Boolean value) {return UIMapResult.success(value);}
        public UIMapResult<Boolean> mapLocalToExternal(Boolean value) {return UIMapResult.success(value);}
    };
    
    public static final UIValueMapper<String, String> STRING_IDENTITY = new UIValueMapper<>() {
        public UIMapResult<String> mapExternalToLocal(String value) {
            return UIMapResult.success(value == null ? "" : value);
        }
        
        public UIMapResult<String> mapLocalToExternal(String value) {
            return UIMapResult.success(value);
        }
    };
    
    public static UIValueMapper<Float, Float> normalisedFloat(float externalMin, float externalMax) {
        return new UIValueMapper<>() {
            public UIMapResult<Float> mapExternalToLocal(Float externalValue) {
                if (externalValue==null) return UIMapResult.failure();
                return UIMapResult.success(Utils.clamp01((externalValue-externalMin)/(externalMax)));
            }
            
            public UIMapResult<Float> mapLocalToExternal(Float localValue) {
                if (localValue==null) return UIMapResult.failure();
                return UIMapResult.success(Utils.lerpFloat(externalMin, externalMax, Utils.clamp01(localValue)));
            }
        };
    }
    
    public UIMapResult<L> mapExternalToLocal(E externalValue);
    public UIMapResult<E> mapLocalToExternal(L localValue);
}
