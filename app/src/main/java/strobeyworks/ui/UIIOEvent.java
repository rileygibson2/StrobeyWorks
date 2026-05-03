package strobeyworks.ui;

public class UIIOEvent {

    public enum UIIOEventType {
        LEFT_PRESS,
        LEFT_RELEASE,
        RIGHT_PRESS,
        RIGHT_RELEASE,
        DRAG,
        KEY_DOWN,
    }

    public UIIOEventType eventType;
    public float mouseX;
    public float mouseY;
    public long keyCode;

    public UIIOEvent(UIIOEventType eventType, float mouseX, float mouseY) {
        this.eventType = eventType;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public UIIOEventType getEventType() {return this.eventType;}

    public float getMouseX() {return mouseX;}

    public float getMouseY() {return mouseY;}
}
