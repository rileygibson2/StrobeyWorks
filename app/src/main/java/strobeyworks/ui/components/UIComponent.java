package strobeyworks.ui.components;

import strobeyworks.ui.core.UIPair;
import strobeyworks.ui.primitives.UIRectangle;

public abstract class UIComponent extends UIRectangle {
    
    private boolean initialised;

    public UIComponent(UIPair width, UIPair height) {
        super(width, height);
        initialised = false;
    }

    public UIComponent() {
        super();
        initialised = false;
    }

    public abstract void initialise();

    public void setInitialised(boolean initialised) {
        this.initialised = initialised;
    }

    public void initialiseIfNeeded() {
        if (initialised) return;
        initialise();
        setInitialised(true);
    }

    public boolean isInitialised() {return this.initialised;}
}
