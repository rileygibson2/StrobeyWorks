package strobeyworks.ui.core;

import java.util.HashSet;
import java.util.Set;

import strobeyworks.logger.Logger;

public class UIFontManager {
    
    private static final Set<UIFont> loaded = new HashSet<>();

    public static UIFont loadFont(String ttfName, float fontSize) {
        UIFont font = UIFont.loadFromTTF(ttfName, fontSize);
        if (font==null) Logger.throwRuntimeException("Could not load UIFont: "+ttfName+"-"+fontSize);

        loaded.add(font);
        return font;
    }
    
    public static UIFont getUIFont(String ttfName, float fontSize) {
        UIFont font = getLoadedUIFont(ttfName, fontSize);
        if (font!=null) return font;

        font = loadFont(ttfName, fontSize);
        if (font==null) Logger.throwRuntimeException("Could not load UIFont: "+ttfName+"-"+fontSize);
        
        return font;
    }

    private static UIFont getLoadedUIFont(String ttfName, float fontSize) {
        for (UIFont font : loaded) {
            if (font.getTTFName().equals(ttfName)&&font.getFontSize()==fontSize) return font;
        }
        return null;
    }
}
