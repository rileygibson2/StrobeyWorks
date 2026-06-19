package strobeyworks.platform;

public abstract class WindowRenderer implements IOSubscriber {
    
    private Window parentWindow;

    public abstract void initialise();
    
    public abstract void update();
    public abstract void render();

    public abstract void handleWindowResize();

    public abstract void addAnimation(Animation a);
    public abstract void removeAnimation(Animation a);

    public void setParentWindow(Window parentWindow) {
        this.parentWindow = parentWindow;
    }

    public Window getParentWindow() {
        return this.parentWindow;
    }
}
