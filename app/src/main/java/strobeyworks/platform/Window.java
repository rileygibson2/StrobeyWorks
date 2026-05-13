package strobeyworks.platform;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwFocusWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.glfw.GLFW.GLFW_FLOATING;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;


import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import strobeyworks.logger.LogColor;
import strobeyworks.logger.LogColorEnum;
import strobeyworks.logger.Logger;
import strobeyworks.platform.IOEvent.IOEventType;


@LogColor(LogColorEnum.PURPLE)
public class Window {
    
    private boolean initialised;
    private long windowID;
    private int width;
    private int height;
    private GLCapabilities capabilities;
    
    private IO io;
    
    private double fpsTimer;
    private int framesTimer;

    private double fps;
    private long lastFrameTime;
    private double frameDeltaTime;
    private double totalTime;
    private int totalFrameCount;
    
    private String title;
    private String titleData;
    private boolean stayFocussed;
    private float screenPosX;
    private float screenPosY;
    
    private Renderer renderer;
    
    public Window(Renderer renderer, int width, int height, String title) {
        this.renderer = renderer;
        this.width = width;
        this.height = height;
        this.initialised = false;
        this.fpsTimer = 0.0;
        this.framesTimer = 0;
        this.title = title;
        this.stayFocussed = false;
        this.screenPosX = 0.5f;
        this.screenPosY = 0.5f;
        
        renderer.setParentWindow(this);
    }

    public void stayFocussed() {
        this.stayFocussed = true;
    }

    public void setScreenPos(float screenPosX, float screenPosY) {
        this.screenPosX = screenPosX;
        this.screenPosY = screenPosY;
    }
    
    public void initialise() {
        // print GLFW errors to stderr
        GLFWErrorCallback.createPrint(System.err).set();
        
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        
        // Window hints (OpenGL version + behavior)
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        
        // Create window
        glfwWindowHint(GLFW_FLOATING, stayFocussed ? GLFW_TRUE : GLFW_FALSE);

        windowID = glfwCreateWindow(width, height, title, NULL, NULL);
        if (windowID == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        // Setup IO

        io = new IO(this, windowID);
        io.setupCallbacks();
        for (IOEventType type : IOEventType.values()) io.subscribe(type, renderer);
        
        // Position window
        long monitor = glfwGetPrimaryMonitor();
        var vid = glfwGetVideoMode(monitor);
        glfwSetWindowPos(
            windowID,
            (int) ((vid.width()-width)*screenPosX),
            (int) ((vid.height()-height)*screenPosY)
        );
        
        // Make context current
        glfwMakeContextCurrent(windowID);
        
        // Enable vsync
        glfwSwapInterval(0);
        
        // Show window
        glfwShowWindow(windowID);
        
        // Load OpenGL bindings (VERY IMPORTANT)
        capabilities = GL.createCapabilities();
        
        glEnable(GL_DEPTH_TEST);
        
        // Setup initial viewport
        glViewport(0, 0, width, height);
        
        // Handle resize
        glfwSetFramebufferSizeCallback(windowID, (win, w, h) -> {
            this.width = w;
            this.height = h;
            glViewport(0, 0, w, h);
            if (renderer!=null) renderer.handleWindowResize();
        });
        
        lastFrameTime = System.nanoTime();
        totalTime = 0L;
        totalFrameCount = 0;
        
        renderer.initialise();
        initialised = true;
    }
    
    public void iterate() {
        // Check time
        long now = System.nanoTime();
        frameDeltaTime = (now - lastFrameTime) * 1e-9;
        lastFrameTime = now;
        totalTime += frameDeltaTime;
        totalFrameCount++;

        fpsTimer += frameDeltaTime;
        framesTimer++;
        
        // Make this window's context current
        glfwMakeContextCurrent(windowID);
        GL.setCapabilities(capabilities);
        glViewport(0, 0, width, height);
        
        
        // FPS calcs
        if (fpsTimer >= 1.0) {
            fps = framesTimer/fpsTimer;

            String s = title + " | " + (int) Math.floor(fps) + " FPS";
            if (titleData != null) s += " - " + titleData;
            glfwSetWindowTitle(windowID, s);
            
            fpsTimer = 0.0;
            framesTimer = 0;
        }
        
        
        if (io!=null) io.update();
        
        renderer.update();
        renderer.render();
        
        if (io!=null) io.endFrame();
        glfwSwapBuffers(windowID);
    }

    public void focus() {
        glfwFocusWindow(windowID);
    }

    public long getWindowID() {
        return windowID;
    }
    
    public Renderer getRenderer() {return this.renderer;}
    
    public IO getIO() {return io;}
    
    public void setWindowDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public int getWidth() {return width;}
    
    public int getHeight() {return height;}
    
    public float getAspectRatio() {return (float) width/(float) height;} 
    
    public void setTitleData(String data) {titleData = data;}
    
    public double getApproxFPS() {return fps;}

    public double getFrameDeltaTime() {return frameDeltaTime;}

    public double getTotalTime() {return totalTime;}

    public double getTotalFrameCount() {return totalFrameCount;}
    
    public boolean windowAlive() {
        return initialised && !glfwWindowShouldClose(windowID);
    }
    
    public void cleanup() {
        Logger.info("Cleaning up");
        glfwDestroyWindow(windowID);
        glfwTerminate();
        GLFWErrorCallback cb = glfwSetErrorCallback(null);
        if (cb != null) cb.free();
    }
}
