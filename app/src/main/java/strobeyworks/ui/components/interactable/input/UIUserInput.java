package strobeyworks.ui.components.interactable.input;

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
import strobeyworks.ui.components.interactable.UIInteractableComponent;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UIPair;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.utils.Vec4;

public class UIUserInput<T> extends UIInteractableComponent<T, String> {
    
    private UIInputRule<T> inputRule;
    private UIText textElem;
    private UIRectangle cursor;
    private Animation flash;
    
    private int cursorPos;
    private Vec4 cachedColor;
    private boolean invalidInput;
    
    public UIUserInput(UIPair width, UIPair height, UIFont font, UIInputRule<T> inputRule) {
        super(width, height, inputRule);
        this.inputRule = inputRule;

        setFocussable(true);
        
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
    public void gotFocus(IOEvent event) {
        float internalX = event.getMouseX()-textElem.getResolvedX();
        cursorPos = textElem.getFont().getCursorIndexAt(getLocalValue(), internalX);
        
        repositionCursor();
        UIRenderer.getInstance().addAnimation(flash);
    }

    @Override
    public void lostFocus(IOEvent event) {
        UIRenderer.getInstance().removeAnimation(flash);
        cursor.visible(false);
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
            repositionCursor();
        }
        
        if (keyCode == GLFW_KEY_RIGHT) {
            cursorPos = Math.min(cursorPos+1, getLocalValue().length());
            repositionCursor();
        }
        
        if (keyCode == GLFW_KEY_BACKSPACE) handleBackSpace();
        
        if (keyCode == GLFW_KEY_ENTER) {
            boolean success = commitLocalValue();
            if (!success) {
                if (!invalidInput) cachedColor = textElem.getColor(); // Protect against multiple failed attempts in a row
                invalidInput = true;
                textElem.color(col(UIColors.RED));
            }

            cursorPos = getLocalValue().length();
            repositionCursor();
        }
    }
    
    private void handleBackSpace() {
        if (invalidInput) {
            textElem.color(cachedColor);
            cachedColor = null;
            invalidInput = false;
        }

        if (cursorPos==0) return;
        String localValue = getLocalValue();
        String left = localValue.substring(0, cursorPos-1);
        String right = localValue.substring(cursorPos);
        
        cursorPos = Math.max(cursorPos-1, 0);
        setLocalValue(left+right);
        repositionCursor();
    }
    
    private void handleCharTyped(char c) {
        if (invalidInput) {
            textElem.color(cachedColor);
            cachedColor = null;
            invalidInput = false;
        }

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
