package strobeyworks.ui.components;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UIPair.pch;
import static strobeyworks.ui.core.UIPair.pcw;
import static strobeyworks.ui.core.UIPair.px;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;

import strobeyworks.logger.Logger;
import strobeyworks.platform.Animation;
import strobeyworks.platform.IOEvent;
import strobeyworks.platform.IOEvent.IOEventType;
import strobeyworks.platform.IOSubscriber;
import strobeyworks.platform.Animation.AnimationForm;
import strobeyworks.ui.UIRenderer;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UIPair;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;

public class UITextInput<T> extends UIInteractableComponent<T> implements IOSubscriber {
    
    private UIText textElem;
    private UIRectangle cursor;
    private Animation flash;
    
    private String text;
    private int cursorPos;
    
    public UITextInput(UIPair width, UIPair height, UIFont font) {
        super(width, height);
        
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
        .cornerRadius(10f);
        
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
        //if (!isBound()) setValue();
        //implementValueOnUI();
        
        // Set cursor height
        float tH = textElem.getResolvedTextHeight();
        float r = getMeasuredHeight();
        cursor.height(px(tH));
        cursor.offsetTop(px((int) ((r-tH)*0.5)));
    }
    
    @Override
    protected void implementValueOnUI() {
        textElem.setText(String.valueOf(getValue()));
    }
    
    @Override
    public void receiveIOEvent(IOEvent event) {
        handleIOEvent(event);
    }
    
    @Override
    public boolean handleIOEvent(IOEvent event) {
        switch (event.getEventType()) {
            case LEFT_PRESS :
            handleGotFocus();
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
    
    public void setText(String text) {
        this.text = text;
        textElem.setText(text);
    }
    
    private void handleGotFocus() {
        UIRenderer.getInstance().addAnimation(flash);
        cursorPos = text.length();
        repositionCursor();
    }
    
    private void handleKeyDown(int keyCode) {
        if (keyCode == GLFW_KEY_LEFT) {
            cursorPos = Math.max(cursorPos-1, 0);
            repositionCursor();
        }
        
        if (keyCode == GLFW_KEY_RIGHT) {
            cursorPos = Math.min(cursorPos+1, text.length());
            repositionCursor();
        }
        
        if (keyCode == GLFW_KEY_BACKSPACE) handleBackSpace();
        
    }

    private void handleBackSpace() {
        if (cursorPos==0) return;
        String left = text.substring(0, cursorPos-1);
        String right = text.substring(cursorPos);
        setText(left+right);
        cursorPos--;
        repositionCursor();
    }
    
    private void handleCharTyped(char c) {
        Logger.debug(c);
        String left = text.substring(0, cursorPos);
        String right = text.substring(cursorPos);
        String nS = left+c+right;
        
        Logger.debug(left+"-"+c+"-"+right+" ["+cursorPos+"]");
        
        setText(nS);
        cursorPos++;
        
        repositionCursor();
    }
    
    private void repositionCursor() {
        float x = textElem.getFont().measureTextWidth(text.substring(0, cursorPos));
        cursor.offsetLeft(px(x+resolveLocal(getPadding().left)));
    }
}
