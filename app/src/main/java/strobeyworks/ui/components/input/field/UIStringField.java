package strobeyworks.ui.components.input.field;

import strobeyworks.platform.IOEvent;
import strobeyworks.ui.components.input.UIValueMapper;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UIRenderer;

public class UIStringField extends UIField<String> {
    
    public UIStringField(UIFont font) {
        super(font, UIValueMapper.STRING_IDENTITY);
        setRegex(".*");
    }
    
    @Override
    public void lostFocus(IOEvent event) {
        handleCommit();
        UIRenderer.getInstance().removeAnimation(flash);
        cursor.style("visible", false);
        super.lostFocus(event);
    }
    
    @Override
    protected String getDefaultLocalValue() {
        return "";
    } 
}
