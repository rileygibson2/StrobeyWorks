package strobeyworks.ui.components.input.field;

import strobeyworks.utils.Utils;

public class UIFloatFieldRule implements UIFieldRule<Float>  {
    
    // Input rules
    private int maxCharacters;
    
    // Validation rules
    private int maxPrecision;
    private float inputMin;
    private float inputMax;
    private float mappedMin;
    private float mappedMax;
    
    protected UIFloatFieldRule() {
        this.maxCharacters = Integer.MAX_VALUE;
        this.maxPrecision = Integer.MAX_VALUE;
        this.inputMin = 0f;
        this.inputMax = 1f;
        this.mappedMin = 0f;
        this.mappedMax = 1f;
    }
    
    public UIFloatFieldRule maxCharacters(int maxCharacters) {
        this.maxCharacters = maxCharacters;
        return this;
    }
    
    public UIFloatFieldRule maxPrecision(int maxPrecision) {
        this.maxPrecision = maxPrecision;
        return this;
    }
    
    public UIFloatFieldRule inputMinMax(float inputMin, float inputMax) {
        this.inputMin = inputMin;
        this.inputMax = inputMax;
        mappedMinMax(inputMin, inputMax); // In case not set
        return this;
    }
    
    public UIFloatFieldRule mappedMinMax(float mappedMin, float mappedMax) {
        this.mappedMin = mappedMin;
        this.mappedMax = mappedMax;
        return this;
    }
    
    @Override
    public String adaptExternalToLocal(Float externalValue) {
        if (externalValue==null) return "null";
        
        float f = mappedToLocal(externalValue);
        f = Math.max(inputMin, Math.min(inputMax, f));
        f = Utils.roundToDp(f, maxPrecision);
        String s = String.valueOf(f);
        
        // If a whole number, don't show dp
        if (Utils.isWhole(f)) s = s.substring(0, s.indexOf("."));
        return s;
    }
    
    @Override
    public Float adaptLocalToExternal(String localValue) {
        try {
            float f = Float.parseFloat(localValue);
            f = Math.max(inputMin, Math.min(inputMax, f));
            f = Utils.roundToDp(f, maxPrecision);
            f = localToMapped(f);
            return f;
        }
        catch (NumberFormatException e) {
            return null;
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
    
    @Override
    public boolean inputFilter(String s) {
        if (s.length()>maxCharacters) return false;
        
        for (int i=0; i<s.length(); i++) {
            if (!acceptsChar(s.charAt(i))) return false;
        }
        return true;
    }
    
    @Override
    public boolean acceptsChar(char c) {
        return String.valueOf(c).matches("[0-9.]");
    }
}