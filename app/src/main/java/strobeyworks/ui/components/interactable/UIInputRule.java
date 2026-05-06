package strobeyworks.ui.components.interactable;

import strobeyworks.logger.Logger;
import strobeyworks.utils.Utils;

public interface UIInputRule<T> extends UIInteractableAdaptor<T, String> {
    
    boolean draftValid(String s);
    T validate(String s);
    boolean acceptsChar(char c);
    
    public static UIInputRule<String> stringInput() {
        return new UIStringInputRule(Integer.MAX_VALUE, ".*");
    }
    
    public static UIInputRule<String> stringInput(int maxCharacters, String whitelist) {
        return new UIStringInputRule(maxCharacters, whitelist);
    }
    
    public static UIInputRule<Float> floatInput() {
        return new UIFloatInputRule(Integer.MAX_VALUE, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Integer.MAX_VALUE);
    }
    
    public static UIInputRule<Float> floatInput(int maxCharacters, float min, float max, int maxPrecision) {
        return new UIFloatInputRule(maxCharacters, min, max, maxPrecision);
    }
}

class UIFloatInputRule implements UIInputRule<Float>  {
    
    // Input rules
    private final int maxCharacters;
    
    // Validation rules
    private final int maxPrecision;
    private final float min;
    private final float max;
    
    public UIFloatInputRule(int maxCharacters, float min, float max, int maxPrecision) {
        this.maxCharacters = maxCharacters;
        this.min = min;
        this.max = max;
        this.maxPrecision = maxPrecision;
    }
    
    @Override
    public String adaptBoundToLocal(Float boundValue) {
        if (boundValue==null) return "null";
        float f = Utils.roundToDp(boundValue, maxPrecision);
        f = Math.max(min, Math.min(max, f));
        return String.valueOf(Utils.roundToDp(f, maxPrecision));
    }
    
    @Override
    public Float adaptLocalToBound(String localValue) {
        try {
            float f = Utils.roundToDp(Float.parseFloat(localValue), maxPrecision);
            Logger.debug(localValue);
            Logger.debug(f);
            Logger.debug(Math.max(min, Math.min(max, f)));
            return Math.max(min, Math.min(max, f));
        }
        catch (NumberFormatException e) {
            return null;
        }
    }
    
    @Override
    public Float validate(String s) {
        try {
            float f = Float.parseFloat(s);
            if (f<min||f>max) return false;
            
            // Check precision
            int dot = s.indexOf('.');
            if (dot==-1 || s.length()-dot-1<=maxPrecision);
        }
        catch (NumberFormatException e) {}
        return min;
    }
    
    @Override
    public boolean draftValid(String s) {
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

class UIStringInputRule implements UIInputRule<String>  {
    
    private final int maxCharacters;
    private final String charRegex;
    
    //"[^0-9]+" Multiple non numbers
    
    public UIStringInputRule(int maxCharacters, String charRegex) {
        this.maxCharacters = maxCharacters;
        this.charRegex = charRegex;
    }
    
    @Override
    public String adaptBoundToLocal(String boundValue) {
        if (boundValue==null) return "null";
        return boundValue;
    }
    
    @Override
    public String adaptLocalToBound(String localValue) {
        return localValue;
    }

    @Override
    public boolean commitValid(String s) {
        return true;
    }
    
    @Override
    public boolean draftValid(String s) {
        if (s.length()>maxCharacters) return false;
        
        for (int i=0; i<s.length(); i++) {
            if (!acceptsChar(s.charAt(i))) return false;
        }
        return true;
    }
    
    @Override
    public boolean acceptsChar(char c) {
        return String.valueOf(c).matches(charRegex);
    }
}
