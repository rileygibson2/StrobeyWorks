package strobeyworks.ui.components.interactable;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UIPair.pch;
import static strobeyworks.ui.core.UIPair.pcw;
import static strobeyworks.ui.core.UIPair.px;

import strobeyworks.platform.Animation;
import strobeyworks.platform.Animation.AnimationForm;
import strobeyworks.platform.IOEvent;
import strobeyworks.platform.IOEvent.IOEventType;
import strobeyworks.platform.IOSubscriber;
import strobeyworks.ui.UIRenderer;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UIPair;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;

public class UIUserInput<T> extends UIInteractableComponent<T, String> implements IOSubscriber {
    
    private UIInputRule<T> inputRule;
    private UIText textElem;
    private UIRectangle cursor;
    private Animation flash;
    
    private int cursorPos;
    
    public UIUserInput(UIPair width, UIPair height, UIFont font, UIInputRule<T> inputRule) {
        super(width, height, inputRule);
        this.inputRule = inputRule;
        
        box(UIBoxMode.FIXED);
        flowDirection(UIFlowDirection.ROW);
        borderColor(col(UIColors.GREEN));
        //justifyContent(UIJustifyContent.CENTER);
        alignItems(UIAlignItems.CENTER);
        paddingLeft(px(10));
        color(col(UIColors.TRANSPARENT));
        cornerRadius(10f);
        
        cursor = new UIRectangle(px(2), pch(0.9f));
        cursor.position(UIPositionMode.ABSOLUTE)
        .offsetLeft(pcw(0.1f))
        .offsetTop(pch(0.1f));
        cursor.color(col(UIColors.GREEN))
        .cornerRadius(10f)
        .visible(false);
        
        textElem = new UIText(font);
        textElem.color(col(UIColors.GREEN));
        
        addChild(textElem);
        addChild(cursor);
        
        cursorPos = 0;
        
        flash = new Animation(1, (i, v) -> {
            if (v>=0.5f) cursor.visible(true);
            if (v<=0.5f) cursor.visible(false);
        });
        flash.setForm(AnimationForm.SINE)
        .setSpeed(1f);
    }
    
    @Override
    public void initialise() {
        // Set cursor height
        float tH = textElem.getResolvedTextHeight();
        float r = getMeasuredHeight();
        cursor.height(px(tH));
        cursor.offsetTop(px((int) ((r-tH)*0.5)));
        
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
        repositionCursor();
    }
    
    @Override
    public void receiveIOEvent(IOEvent event) {
        handleIOEvent(event);
    }
    
    @Override
    public boolean handleIOEvent(IOEvent event) {
        switch (event.getEventType()) {
            case LEFT_PRESS :
            handleGotFocus(event);
            event.getIO().subscribe(IOEventType.CHAR_TYPED, this);
            event.getIO().subscribe(IOEventType.KEY_DOWN, this);
            return false;
            
            case KEY_DOWN :
            handleKeyDown(event.getKeyCode());
            return false;
            
            case CHAR_TYPED :
            handleCharTyped((char) event.getKeyCode());
            return false;
            
            default:
            return true;
        }
    }
    
    private void handleGotFocus(IOEvent event) {
        float internalX = event.getMouseX()-textElem.getResolvedX();
        cursorPos = textElem.getFont().getCursorIndexAt(getLocalValue(), internalX);
        
        repositionCursor();
        UIRenderer.getInstance().addAnimation(flash);
    }
    
    private void handleKeyDown(int keyCode) {
        if (keyCode == GLFW_KEY_LEFT) {
            cursorPos = Math.max(cursorPos-1, 0);
            repositionCursor();
        }
        
        if (keyCode == GLFW_KEY_RIGHT) {
            cursorPos = Math.min(cursorPos+1, getLocalValue().length());
            repositionCursor();
        }
        
        if (keyCode == GLFW_KEY_BACKSPACE) handleBackSpace();
        
        if (keyCode == GLFW_KEY_ENTER) {
            if (inputRule.commitValid(getLocalValue())) commitLocalValue();
            cursorPos = getLocalValue().length();
            repositionCursor();
        }
    }
    
    private void handleBackSpace() {
        if (cursorPos==0) return;
        String localValue = getLocalValue();
        String left = localValue.substring(0, cursorPos-1);
        String right = localValue.substring(cursorPos);
        
        setLocalValue(left+right);
        repositionCursor();
    }
    
    private void handleCharTyped(char c) {
        String localValue = getLocalValue();
        String left = localValue.substring(0, cursorPos);
        String right = localValue.substring(cursorPos);
        String nS = left+c+right;

        if (!inputRule.draftValid(nS)) return;
        
        setLocalValue(nS);
        cursorPos++;
        repositionCursor();
    }
    
    private void repositionCursor() {
        float x = textElem.getFont().measureTextWidth(getLocalValue().substring(0, cursorPos));
        cursor.offsetLeft(px(x+resolveLocal(getPadding().left)));
    }
    

}
