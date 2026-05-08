package strobeyworks.platform;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import java.util.EnumMap;
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
    
    private EnumMap<IOEventType, Set<IOSubscriber>> subscribers;
    
    public IO(Window parentWindow, long windowID) {
        this.parentWindow = parentWindow;
        this.windowID = windowID;
        
        subscribers = new EnumMap<>(IOEventType.class);
        for (IOEventType type : IOEventType.values()) subscribers.put(type, new HashSet<>());
    }
    
    public void subscribe(IOEventType eventType, IOSubscriber subscriber) {
        subscribers.get(eventType).add(subscriber);
    }
    
    public void unsubscribe(IOEventType eventType, IOSubscriber subscriber) {
        subscribers.get(eventType).remove(subscriber);
    }
    
    private void publish(IOEvent event) {
        Set<IOSubscriber> snapshot = new HashSet<>(subscribers.get(event.getEventType()));
        for (IOSubscriber s : snapshot) s.receiveIOEvent(event);
    }
    
    public void setupCallbacks() {
        // ESC closes window
        glfwSetKeyCallback(windowID, (win, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(win, true);
            }
            
            if (action == GLFW_PRESS) publish(IOEvent.keyDown(this, key));
            if (action == GLFW_RELEASE) publish(IOEvent.keyUp(this, key));
        });
        
        glfwSetCharCallback(windowID, (win, codepoint) -> {
            publish(IOEvent.charTyped(this, (int) codepoint));
        });
        
        
        glfwSetScrollCallback(windowID, (win, xOffset, yOffset) -> {
            scrollDX += xOffset;
            scrollDY += yOffset;
        });
        
        glfwSetMouseButtonCallback(windowID, (window, button, action, mods) -> {
            double[] mouseX = new double[1];
            double[] mouseY = new double[1];
            glfwGetCursorPos(windowID, mouseX, mouseY);
            
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                leftPressed = true;
                publish(IOEvent.leftPress(this, (float) mouseX[0], (float) mouseY[0]));
            }
            
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_RELEASE) {
                leftPressed = false;
                publish(IOEvent.leftRelease(this, (float) mouseX[0], (float) mouseY[0]));
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
        
        if (mouseX!=prevMouseX||mouseY!=prevMouseY) {
            publish(IOEvent.mouseMove(this, (float) xPos[0], (float) yPos[0]));
        }


        if (leftPressed&&(mouseDX!=0||mouseDY!=0)) {
            publish(IOEvent.drag(this, (float) xPos[0], (float) yPos[0]));
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
