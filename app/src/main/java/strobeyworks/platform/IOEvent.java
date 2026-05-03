package strobeyworks.platform;

public class IOEvent {

    public enum IOEventType {
        LEFT_PRESS,
        LEFT_RELEASE,
        RIGHT_PRESS,
        RIGHT_RELEASE,
        DRAG,
        KEY_DOWN,
    }

    private final IO io;
    private final IOEventType eventType;

    private final float mouseX;
    private final float mouseY;
    private final long keyCode;

    public IOEvent(IO io, IOEventType eventType, float mouseX, float mouseY) {
        this.io = io;
        this.eventType = eventType;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.keyCode = 0L;
    }

    public IO getIO() {return this.io;}

    public IOEventType getEventType() {return this.eventType;}

    public float getMouseX() {return mouseX;}

    public float getMouseY() {return mouseY;}
}
