package strobeyworks.render;

import strobeyworks.Window;

public abstract class Renderer {
    
    private Window parentWindow;

    public Renderer() {}

    public abstract void init();
    public abstract void render();

    public void setParentWindow(Window parentWindow) {this.parentWindow = parentWindow;}
    public Window getParentWindow() {return this.parentWindow;}
}
