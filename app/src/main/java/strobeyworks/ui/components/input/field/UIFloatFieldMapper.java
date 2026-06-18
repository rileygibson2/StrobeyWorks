package strobeyworks.ui.components.input.field;

import strobeyworks.ui.components.input.UIValueMapper;
import strobeyworks.utils.Utils;

public class UIFloatFieldMapper implements UIValueMapper<Float, String>  {
    
    // Validation rules
    private int maxPrecision;
    private float inputMin;
    private float inputMax;
    private float mappedMin;
    private float mappedMax;
    
    public UIFloatFieldMapper() {
        this.maxPrecision = Integer.MAX_VALUE;
        this.inputMin = 0f;
        this.inputMax = 1f;
        this.mappedMin = 0f;
        this.mappedMax = 1f;
    }
    
    public UIFloatFieldMapper inputMinMax(float inputMin, float inputMax) {
        this.inputMin = inputMin;
        this.inputMax = inputMax;
        mappedMinMax(inputMin, inputMax); // In case not set
        return this;
    }
    
    public UIFloatFieldMapper mappedMinMax(float mappedMin, float mappedMax) {
        this.mappedMin = mappedMin;
        this.mappedMax = mappedMax;
        return this;
    }

    public UIFloatFieldMapper maxPrecision(int maxPrecision) {
        this.maxPrecision = maxPrecision;
        return this;
    }
    
    @Override
    public UIMapResult<String> mapExternalToLocal(Float externalValue) {
        if (externalValue==null) return UIMapResult.failure();
        
        float f = mappedToLocal(externalValue);
        f = Math.max(inputMin, Math.min(inputMax, f));
        f = Utils.roundToDp(f, maxPrecision);
        String s = String.valueOf(f);
        
        // If a whole number, don't show dp
        if (Utils.isWhole(f)) s = s.substring(0, s.indexOf("."));
        return UIMapResult.success(s);
    }
    
    @Override
    public UIMapResult<Float> mapLocalToExternal(String localValue) {
        try {
            float f = Float.parseFloat(localValue);
            f = Math.max(inputMin, Math.min(inputMax, f));
            f = Utils.roundToDp(f, maxPrecision);
            f = localToMapped(f);
            return UIMapResult.success(f);
        }
        catch (NumberFormatException e) {
            return UIMapResult.failure();
        }
    }
    
    private float mappedToLocal(float value) {
        float v = (value-mappedMin)/(mappedMax-mappedMin);
        return inputMin+v*(inputMax-inputMin);
    }

    private float localToMapped(float value) {
        float v = (value-inputMin)/(inputMax-inputMin);
        return mappedMin+v*(mappedMax-mappedMin);
    }
}