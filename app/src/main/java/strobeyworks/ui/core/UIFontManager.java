package strobeyworks.ui.core;

import java.util.HashSet;
import java.util.Set;

import strobeyworks.logger.Logger;

public class UIFontManager {
    
    private static final Set<UIFont> loaded = new HashSet<>();

    public static void loadFont(String ttfName, float fontSize) {
        UIFont font = UIFont.loadFromTTF(ttfName, fontSize);
        if (font!=null) loaded.add(font);
    }
    
    public static UIFont getUIFont(String ttfName, float fontSize) {
        for (UIFont font : loaded) {
            if (font.getTTFName().equals(ttfName)&&font.getFontSize()==fontSize) return font;
        }

        Logger.throwRuntimeException("UI font not loaded: "+ttfName+"-"+fontSize);
        return null;
    }
}
