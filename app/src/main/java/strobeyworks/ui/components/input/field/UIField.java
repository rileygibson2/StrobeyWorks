package strobeyworks.ui.components.input.field;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.logger.Logger;
import strobeyworks.platform.Animation;
import strobeyworks.platform.Animation.AnimationForm;
import strobeyworks.platform.IOEvent;
import strobeyworks.ui.components.input.UIBindableInput;
import strobeyworks.ui.components.input.UIValueMapper;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UIRenderer;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.utils.Vec4;

public class UIField<T> extends UIBindableInput<T, String> {
    
    private String regex;
    private int maxCharacters;
    
    protected UIRectangle wrapper;
    private UIText textElem;
    protected UIRectangle cursor;
    protected Animation flash;
    
    private int cursorPos;
    private UIColor cachedColor;
    private boolean invalidInput;

    private UIBasicCallback onCommit;
    
    public UIField(UIFont font, UIValueMapper<T, String> mapper) {
        super(mapper);
        regex = "\"(?s).\"";
        maxCharacters = Integer.MAX_VALUE;
        
        focussable(true);
        
        style("align-items", UIAlignItems.CENTER);
        style("border-enabled", false);
        style("padding-left",px(10));
        style("color", UIColor.rgb(0.3f));
        style("corner-radius", new Vec4(2f));
        style("overflow-x", UIOverflowMode.HIDDEN);
        
        wrapper = new UIRectangle();
        wrapper.style("width", pcw(1f))
        .style("height", pch(1f))
        .style("align-items", UIAlignItems.CENTER);
        
        cursor = new UIRectangle();
        cursor.style("width", px(2))
        .style("height", pch(0.9f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-left", pcw(0.1f))
        .style("offset-top", pch(0.1f))
        .style("color", UIColor.rgb(0.8f))
        .style("corner-radius", new Vec4(10f))
        .style("visible", false);
        
        textElem = new UIText(font);
        textElem.style("color", UIColor.rgb(0.7f));
        
        addChild(wrapper);
        wrapper.addChild(textElem);
        wrapper.addChild(cursor);
        
        cursorPos = 0;
        
        flash = new Animation(1, (i, v) -> {
            if (v>=0.5f) cursor.style("visible", true);
            if (v<=0.5f) cursor.style("visible", false);
        });
        flash.setForm(AnimationForm.SINE)
        .setSpeed(1f);
    }

    public void onCommit(UIBasicCallback onCommit) {
        this.onCommit = onCommit;
    }
    
    @Override
    public void initialise() {
        repositionCursorY();
        super.initialise();
    }
    
    @Override
    protected String getDefaultLocalValue() {
        return "";
    }
    
    @Override
    protected void implementLocalValueOnUI() {
        String localValue = getLocalValue();
        textElem.setText(localValue);
        cursorPos = Math.min(cursorPos, localValue.length()); // Incase external update of value changed length of text
        repositionCursorX();
    }
    
    @Override
    public void gotFocus(IOEvent event) {
        float internalX = event.getMouseX()-textElem.getScreenX();
        cursorPos = textElem.getFont().getCursorIndexAt(getLocalValue(), internalX);
        
        repositionCursorY();
        repositionCursorX();
        UIRenderer.getInstance().addAnimation(flash);
    }
    
    @Override
    public void lostFocus(IOEvent event) {
        UIRenderer.getInstance().removeAnimation(flash);
        cursor.style("visible", false);
        super.lostFocus(event);
    }
    
    @Override
    public void handleIOEvent(IOEvent event) {
        switch (event.getEventType()) {
            case KEY_DOWN :
            handleKeyDown(event.getKeyCode());
            break;
            
            case CHAR_TYPED :
            handleCharTyped((char) event.getKeyCode());
            break;
            
            default: break;
        }
    }
    
    private void handleKeyDown(int keyCode) {
        if (keyCode == GLFW_KEY_LEFT) {
            cursorPos = Math.max(cursorPos-1, 0);
            repositionCursorX();
        }
        else if (keyCode == GLFW_KEY_RIGHT) {
            cursorPos = Math.min(cursorPos+1, getLocalValue().length());
            repositionCursorX();
        }
        else if (keyCode == GLFW_KEY_BACKSPACE) handleBackSpace();
        else if (keyCode == GLFW_KEY_ENTER) handleCommit();
    }
    
    protected void handleCommit() {
        boolean success = commitLocalValue();
        if (!success) {
            if (!invalidInput) cachedColor = textElem.getColor(); // Protect against multiple failed attempts in a row
            invalidInput = true;
            textElem.style("color", UIColor.red());
        }
        
        cursorPos = getLocalValue().length();
        repositionCursorX();
        
        if (onCommit!=null) onCommit.implement();
    }
    
    private void handleBackSpace() {
        if (invalidInput) {
            textElem.style("color", cachedColor);
            cachedColor = null;
            invalidInput = false;
        }
        
        if (cursorPos==0) return;
        String localValue = getLocalValue();
        String left = localValue.substring(0, cursorPos-1);
        String right = localValue.substring(cursorPos);
        
        cursorPos = Math.max(cursorPos-1, 0);
        setLocalValue(left+right);
        repositionCursorX();
    }
    
    private void handleCharTyped(char c) {
        if (invalidInput) {
            textElem.style("color", cachedColor);
            cachedColor = null;
            invalidInput = false;
        }
        
        String localValue = getLocalValue();
        String left = localValue.substring(0, cursorPos);
        String right = localValue.substring(cursorPos);
        String nS = left+c+right;
        
        if (!inputFilter(nS)) return;
        
        setLocalValue(nS);
        cursorPos++;
        repositionCursorX();
    }
    
    private void repositionCursorX() {
        float x = textElem.getFont().measureTextWidth(getLocalValue().substring(0, cursorPos));
        cursor.style("offset-left", px(x+wrapper.resolve(wrapper.getPaddingLeft())));
    }
    
    private void repositionCursorY() {
        float tH = textElem.getResolvedTextHeight();
        float r = wrapper.getScreenHeight();
        
        cursor.style("height", px(tH));
        cursor.style("offset-top", px((r - tH) * 0.5f));
    }
    
    protected UIField<T> setRegex(String regex) {
        this.regex = regex;
        return this;
    }
    
    public UIField<T> setMaxCharacters(int maxCharacters) {
        this.maxCharacters = maxCharacters;
        return this;
    }
    
    private boolean inputFilter(String s) {
        if (s.length()>maxCharacters) return false;
        
        for (int i=0; i<s.length(); i++) {
            if (!acceptsChar(s.charAt(i))) return false;
        }
        return true;
    }
    
    private boolean acceptsChar(char c) {
        return String.valueOf(c).matches(regex);
    }
}
