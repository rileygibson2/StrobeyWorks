package strobeyworks;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwTerminate;

import javax.swing.Timer;

import strobeyworks.logger.Logger;
import strobeyworks.noiserender.AgentRenderer;
import strobeyworks.noiserender.NoiseRenderer;
import strobeyworks.platform.MidiManager;
import strobeyworks.platform.ShaderManager;
import strobeyworks.platform.Window;
import strobeyworks.ui.core.UIRenderer;

public class SWMain {
    
    private static SWMain instance;
    
    private static Window renderWindow;
    private static Window uiWindow;
    private static ShaderManager shaderManager;
    private static MidiManager midiManager;
    
    private static long lastTime;
    private static float deltaTime;
    private static float totalTime;
    private static long totalFrameCount;
    private static boolean running;
    
    public static SWMain getInstance() {
        if (instance==null) instance = new SWMain();
        return instance;
    }
    
    private SWMain() {}
    
    public static ShaderManager getShaderManager() {return shaderManager;}
    
    public static Window getRenderWindow() {return renderWindow;}
    
    public static Window getUIWindow() {return uiWindow;}
    
    private void setup() {
        Logger.info("Setting up");
        shaderManager = new ShaderManager();
        
        //renderWindow = new Window(SceneRenderer.getInstance(), 1500, 900, "Render");
        renderWindow = new Window(AgentRenderer.getInstance(), 1500, 900, "Agent Render");
        
        uiWindow = new Window(UIRenderer.getInstance(), 500, 500, "UI");
        uiWindow.stayFocussed();
        uiWindow.setScreenPos(0.8f, 0.5f);
        
        renderWindow.initialise();
        uiWindow.initialise();
        
        midiManager = MidiManager.getInstance();
        midiManager.open("MIDICRAFT ENC");
    }
    
    private void start() {
        lastTime = System.nanoTime();
        totalFrameCount = 0L;
        totalTime = 0L;
        running = true;
        
        Logger.info("Starting main loop");
        while (running) {
            long now = System.nanoTime();
            deltaTime = Math.min((now - lastTime) * 1e-9f, 0.05f);
            lastTime = now;
            totalTime += deltaTime;
            totalFrameCount++;
            
            glfwPollEvents();

            midiManager.update();
            
            // Exit condition
            if (!renderWindow.windowAlive()||!uiWindow.windowAlive()) running = false;
            else {
                renderWindow.iterate();
                uiWindow.iterate();
            }
        }
        Logger.info("Exiting main loop");
        shutdown();
    }
    
    private void shutdown() {
        MidiManager.getInstance().cleanup();
        if (renderWindow!=null) renderWindow.cleanup();
        if (uiWindow!=null) uiWindow.cleanup();
        glfwTerminate();
        
        
        Logger.info("Shutting down");
        System.exit(0);
    }

    private void logAllOpenThreads() {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.isAlive()&&!t.isDaemon()&&!t.getName().equals("main")) {
                Logger.error("Non-daemon thread still alive: " + t.getName());
            }
        }
    }
    
    public static float getDeltaTime() {return deltaTime;}
    
    public static float getTotalTime() {return totalTime;}
    
    public static long getTotalFrameCount() {return totalFrameCount;}
    
    public static boolean isFrameIncrement(int inc) {return totalFrameCount%inc==0;}
    
    public static void main(String[] args) {
        SWMain sw = SWMain.getInstance();
        sw.setup();
        sw.start();
    }
}