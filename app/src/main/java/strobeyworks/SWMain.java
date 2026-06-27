package strobeyworks;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwTerminate;

import java.util.HashSet;
import java.util.Set;

import strobeyworks.logger.Logger;
import strobeyworks.pipeline.RenderNode;
import strobeyworks.pipeline.RenderPipeline;
import strobeyworks.pipeline.RenderTarget;
import strobeyworks.platform.MidiManager;
import strobeyworks.platform.OutputRenderer;
import strobeyworks.platform.ShaderManager;
import strobeyworks.platform.Window;
import strobeyworks.rendernodes.AgentNode;
import strobeyworks.rendernodes.PerlinNode;
import strobeyworks.ui.core.UIRenderer;
import strobeyworks.utils.Vec2;

public class SWMain {
    
    private static SWMain instance;
    
    private static Window outputWindow;
    private static Window uiWindow;
    private static OutputRenderer outputRenderer;
    
    private static RenderPipeline pipeline;
    
    private MidiManager midiManager;
    
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
    
    private void setup() {
        Logger.info("Setting up");
        
        // Managers
        ShaderManager.getInstance();
        midiManager = MidiManager.getInstance();
        midiManager.open("MIDICRAFT ENC");
        
        // Windows
        outputRenderer = new OutputRenderer();
        outputWindow = new Window(outputRenderer, 1500, 900, "Output");
        
        uiWindow = new Window(UIRenderer.getInstance(), 1100, 700, "UI");
        uiWindow.stayFocussed();
        uiWindow.setScreenPos(0.8f, 0.5f);
        
        outputWindow.initialise();
        outputWindow.makeContextCurrent();
        
        // Pipeline
        pipeline = RenderPipeline.getInstance();
        //pipeline.simulate();
        pipeline.loadPipelineFromDisk();
        
        // UI initialise
        uiWindow.shareContextWith(outputWindow);
        uiWindow.initialise();
    }
    
    public static void windowResized(Window window) {
        if (pipeline == null) return;
        window.makeContextCurrent();
        
        if (window==outputWindow) {
            for (RenderNode n : pipeline.getAllNodes()) {
                n.resizeOutput(window.getFramebufferDimensions());
            }
        }
    }

    public static Window getOutputWindow() {return outputWindow;}

    public static OutputRenderer getOutputWindowRenderer() {return outputRenderer;}
    
    public static Window getUIWindow() {return uiWindow;}
    
    private void start() {
        lastTime = System.nanoTime();
        totalFrameCount = 0L;
        totalTime = 0L;
        running = true;
        
        Logger.info("Starting main loop");
        while (running) {
            // Exit condition
            if (!outputWindow.windowAlive()||!uiWindow.windowAlive()) {
                running = false;
                break;
            }
            
            // Times
            long now = System.nanoTime();
            deltaTime = Math.min((now - lastTime) * 1e-9f, 0.05f);
            lastTime = now;
            totalTime += deltaTime;
            totalFrameCount++;
            
            // Updates
            glfwPollEvents();
            midiManager.update();
            
            // Nodes
            outputWindow.makeContextCurrent();
            pipeline.iterate();
            
            // Windows
            outputWindow.iterate();
            uiWindow.iterate();
        }
        Logger.info("Exiting main loop");
        shutdown();
    }
    
    private void shutdown() {
        // Managers
        MidiManager.getInstance().cleanup();
        
        // Output targets
        outputWindow.makeContextCurrent();
        pipeline.cleanup();
        
        // Windows
        if (outputWindow!=null) outputWindow.cleanup();
        if (uiWindow!=null) uiWindow.cleanup();
        glfwTerminate();
        
        Logger.info("Shutting down");
        System.exit(0);
    }
    
    @SuppressWarnings("unused")
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