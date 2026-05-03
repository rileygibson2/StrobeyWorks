package strobeyworks.platform;

public abstract class Renderer {
    
    private Window parentWindow;

    public Renderer() {}

    public abstract void init();
    public abstract void render();

    public abstract void handleIOEvent(IOEvent event);

    public abstract void addAnimation(Animation a);
    public abstract void removeAnimation(Animation a);

    public void setParentWindow(Window parentWindow) {this.parentWindow = parentWindow;}
    public Window getParentWindow() {return this.parentWindow;}
}
