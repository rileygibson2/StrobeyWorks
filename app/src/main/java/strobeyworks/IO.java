package strobeyworks;

import static org.lwjgl.glfw.GLFW.*;

public class IO {
    
    private Window parentWindow;
    private long windowID;
    
    public double mouseX;
    public double mouseY;
    private double prevMouseX;
    private double prevMouseY;
    public double mouseDY;
    public double mouseDX;
    
    public boolean leftPressed;
    public boolean rightPressed;
    
    public double scrollDX;
    public double scrollDY;
    
    public IO(Window parentWindow, long windowID) {
        this.parentWindow = parentWindow;
        this.windowID = windowID;
    }
    
    public void setupCallbacks() {
        // ESC closes window
        glfwSetKeyCallback(windowID, (win, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(win, true);
            }
        });
        
        glfwSetScrollCallback(windowID, (win, xOffset, yOffset) -> {
            scrollDX += xOffset;
            scrollDY += yOffset;
        });
        
    }
    
    public void update() {
        prevMouseX = mouseX;
        prevMouseY = mouseY;
        
        double[] xPos = new double[1];
        double[] yPos = new double[1];
        
        glfwGetCursorPos(windowID, xPos, yPos);
        
        mouseX = xPos[0];
        mouseY = parentWindow.getHeight()-yPos[0];
        mouseDX = mouseX-prevMouseX;
        mouseDY = mouseY-prevMouseY;
        
        leftPressed = glfwGetMouseButton(windowID, GLFW_MOUSE_BUTTON_LEFT)==GLFW_PRESS;
        rightPressed = glfwGetMouseButton(windowID, GLFW_MOUSE_BUTTON_RIGHT)==GLFW_PRESS;
    }

    public void endFrame() {
        scrollDX = 0;
        scrollDY = 0;
        mouseDX = 0;
        mouseDY = 0;
    }
    
    public boolean mouseDown() {return leftPressed||rightPressed;}
    
    public boolean keyDown(int key) {return glfwGetKey(windowID, key) == GLFW_PRESS;}
    
}
