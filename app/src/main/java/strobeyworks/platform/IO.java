package strobeyworks.platform;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import java.util.HashSet;
import java.util.Set;

import strobeyworks.platform.IOEvent.IOEventType;

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
    
    private Set<IOSubscriber> subscribers;
    
    public IO(Window parentWindow, long windowID) {
        this.parentWindow = parentWindow;
        this.windowID = windowID;
        subscribers = new HashSet<>();
    }
    
    public void subscribe(IOSubscriber subscriber) {
        subscribers.add(subscriber);
    }
    
    public void unsubscribe(IOSubscriber subscriber) {
        subscribers.remove(subscriber);
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
            glfwGetCursorPos(windowID, mouseX, mouseY);
            IOEvent event = null;
            
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                leftPressed = true;
                event = new IOEvent(
                    this,
                    IOEventType.LEFT_PRESS,
                    (float) mouseX[0],
                    (float) mouseY[0]
                );
            }
            
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_RELEASE) {
                leftPressed = false;
                event = new IOEvent(
                    this,
                    IOEventType.LEFT_RELEASE,
                    (float) mouseX[0],
                    (float) mouseY[0]
                );
            }
            
            if (event!=null) {
                parentWindow.getRenderer().handleIOEvent(event);
                for (IOSubscriber s : subscribers) s.receiveIOEvent(event);
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
            IOEvent event = new IOEvent(
                this,
                IOEventType.DRAG,
                (float) xPos[0],
                (float) yPos[0]
            );
            
            for (IOSubscriber s : subscribers) s.receiveIOEvent(event);
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
