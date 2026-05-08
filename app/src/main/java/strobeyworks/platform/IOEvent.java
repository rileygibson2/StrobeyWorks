package strobeyworks.platform;

public class IOEvent {

    public enum IOEventType {
        LEFT_PRESS,
        LEFT_RELEASE,
        RIGHT_PRESS,
        RIGHT_RELEASE,
        DRAG,
        MOUSE_MOVE,
        KEY_DOWN,
        KEY_UP,
        CHAR_TYPED
    }

    private final IO io;
    private final IOEventType eventType;

    private final float mouseX;
    private final float mouseY;
    private final int keyCode;

    private IOEvent(IO io, IOEventType eventType, float mouseX, float mouseY, int keyCode) {
        this.io = io;
        this.eventType = eventType;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.keyCode = keyCode;
    }

    public IO getIO() {return this.io;}

    public IOEventType getEventType() {return this.eventType;}

    public float getMouseX() {return mouseX;}

    public float getMouseY() {return mouseY;}

    public int getKeyCode() {return keyCode;}

    public static IOEvent leftPress(IO io, float mouseX, float mouseY) {
        return new IOEvent(io, IOEventType.LEFT_PRESS, mouseX, mouseY, -1);
    }

    public static IOEvent leftRelease(IO io, float mouseX, float mouseY) {
        return new IOEvent(io, IOEventType.LEFT_RELEASE, mouseX, mouseY, -1);
    }

    public static IOEvent rightPress(IO io, float mouseX, float mouseY) {
        return new IOEvent(io, IOEventType.RIGHT_PRESS, mouseX, mouseY, -1);
    }

    public static IOEvent rightRelease(IO io, float mouseX, float mouseY) {
        return new IOEvent(io, IOEventType.RIGHT_RELEASE, mouseX, mouseY, -1);
    }

    public static IOEvent drag(IO io, float mouseX, float mouseY) {
        return new IOEvent(io, IOEventType.DRAG, mouseX, mouseY, -1);
    }

    public static IOEvent mouseMove(IO io, float mouseX, float mouseY) {
        return new IOEvent(io, IOEventType.MOUSE_MOVE, mouseX, mouseY, -1);
    }

    public static IOEvent keyDown(IO io, int keyCode) {
        return new IOEvent(io, IOEventType.KEY_DOWN, -1, -1, keyCode);
    }

    public static IOEvent keyUp(IO io, int keyCode) {
        return new IOEvent(io, IOEventType.KEY_UP, -1, -1, keyCode);
    }

    public static IOEvent charTyped(IO io, int keyCode) {
        return new IOEvent(io, IOEventType.CHAR_TYPED, -1, -1, keyCode);
    }
}
