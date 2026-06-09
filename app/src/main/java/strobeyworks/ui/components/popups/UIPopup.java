package strobeyworks.ui.components.popups;

import java.util.HashSet;
import java.util.Set;

import strobeyworks.ui.primitives.UIRectangle;

public abstract class UIPopup extends UIRectangle {
    
    public Set<UISuccessCallback> closedActions;

    public UIPopup() {
        closedActions = new HashSet<>();
    }

    public void addClosedAction(UISuccessCallback closedAction) {
        this.closedActions.add(closedAction);
    }
    
    public void close(boolean success) {
        for (UISuccessCallback a : closedActions) a.implement(success);
        if (hasParent()) getParent().removeChild(this);
    }
}
