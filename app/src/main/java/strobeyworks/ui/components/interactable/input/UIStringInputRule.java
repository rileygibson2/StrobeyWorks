package strobeyworks.ui.components.interactable.input;

public class UIStringInputRule implements UIInputRule<String>  {
    
    private int maxCharacters;
    private String charRegex;
    
    //"[^0-9]+" Multiple non numbers
    
    public UIStringInputRule() {
        this.maxCharacters = Integer.MAX_VALUE;
        this.charRegex = ".*"; // Match everything
    }

    public UIStringInputRule maxCharacters(int maxCharacters) {
        this.maxCharacters = maxCharacters;
        return this;
    }

    public UIStringInputRule charRegex(String charRegex) {
        this.charRegex = charRegex;
        return this;
    }
    
    @Override
    public String adaptExternalToLocal(String externalValue) {
        if (externalValue==null) return "null";
        return externalValue;
    }
    
    @Override
    public String adaptLocalToExternal(String localValue) {
        return localValue;
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