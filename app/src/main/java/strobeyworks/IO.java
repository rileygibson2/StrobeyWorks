package strobeyworks;

import static org.lwjgl.glfw.GLFW.*;

import strobeyworks.ui.UIIOEvent;
import strobeyworks.ui.UIIOEvent.UIIOEventType;

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
        
        glfwSetMouseButtonCallback(windowID, (window, button, action, mods) -> {
            double[] mouseX = new double[1];
            double[] mouseY = new double[1];
            glfwGetCursorPos(window, mouseX, mouseY);
            
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                leftPressed = true;
                
                parentWindow.getRenderer().handleIOEvent(new UIIOEvent(
                    UIIOEventType.LEFT_PRESS,
                    (float) mouseX[0],
                    (float) mouseY[0]
                ));
            }
            
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_RELEASE) {
                leftPressed = false;
                
                parentWindow.getRenderer().handleIOEvent(new UIIOEvent(
                    UIIOEventType.LEFT_RELEASE,
                    (float) mouseX[0],
                    (float) mouseY[0]
                ));
            }
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
        
        if (leftPressed && (mouseDX != 0 || mouseDY != 0)) {
            parentWindow.getRenderer().handleIOEvent(new UIIOEvent(
                UIIOEventType.DRAG,
                (float) xPos[0],
                (float) yPos[0]
            ));
        }
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
