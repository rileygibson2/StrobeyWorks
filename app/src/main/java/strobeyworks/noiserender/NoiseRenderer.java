package strobeyworks.noiserender;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import strobeyworks.SWMain;
import strobeyworks.platform.Animation;
import strobeyworks.platform.IOEvent;
import strobeyworks.platform.Renderer;
import strobeyworks.platform.ShaderManager;
import strobeyworks.utils.Bindable;
import strobeyworks.utils.Utils;
import strobeyworks.utils.Vec3;

public class NoiseRenderer extends Renderer {
    
    private static NoiseRenderer instance;
    
    private int noiseProgram;
    private int quadVAO;
    
    private Bindable<Float> speed;
    private Bindable<Float> gridSize;

    private Bindable<Float> octaves;

    private Bindable<Float> gamma;
    private Bindable<Float> gain;

    private Bindable<Boolean> warp;
    private Bindable<Float> warpStrength;
    private Bindable<Float> warpScale;

    private Bindable<Boolean> octaveRidge;
    private Bindable<Boolean> postRidge;
    private Bindable<Float> ridgePow;

    private Bindable<Boolean> octaveTurbulence;
    private Bindable<Float> turbulencePow;

    private Vec3 colorLow;
    private Vec3 colorHigh;
    
    public static NoiseRenderer getInstance() {
        if (instance==null) instance = new NoiseRenderer();
        return instance;
    }
    
    private NoiseRenderer() {
        this.speed = Bindable.of(0.5f);
        this.gridSize = Bindable.of(10f);
        this.octaves = Bindable.of(1f);
        this.gamma = Bindable.of(1f);
        this.gain = Bindable.of(1f);

        this.warp = Bindable.of(false);
        this.warpStrength = Bindable.of(0f);
        this.warpScale = Bindable.of(1f);

        this.octaveRidge = Bindable.of(false);
        this.postRidge = Bindable.of(false);
        this.ridgePow = Bindable.of(2f);

        this.octaveTurbulence = Bindable.of(false);
        this.turbulencePow = Bindable.of(2f);

        this.colorLow = new Vec3(0f, 0f, 0f);
        this.colorHigh = new Vec3(1f, 0f, 0.8f);
    }
    
    @Override
    public void receiveIOEvent(IOEvent event) {}
    
    @Override
    public void handleWindowResize() {}
    
    @Override
    public void addAnimation(Animation a) {}
    
    @Override
    public void removeAnimation(Animation a) {}
    
    @Override
    public void initialise() {
        ShaderManager sM = SWMain.getShaderManager();
        
        // Shaders init
        noiseProgram = sM.createProgram("noise/perlinfbm.vert", "noise/perlinfbm.frag");
        quadVAO = glGenVertexArrays();
        int quadVBO = glGenBuffers();
        sM.bindVAO(quadVAO);
        sM.bindVBO(quadVBO);
        glBufferData(GL_ARRAY_BUFFER, ShaderManager.QUAD_VERTICES, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        sM.bindVAO(0);
        sM.bindVBO(0);
        sM.useProgram(0);
    }
    
    @Override
    public void update() {
        
    }
    
    @Override
    public void render() {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        renderPass();
    }
    
    private void renderPass() {
        ShaderManager sM = SWMain.getShaderManager();
        sM.useProgram(noiseProgram);
        sM.setCurrentProgram(noiseProgram);

        double gridSizeV = Math.ceil(gridSize.getValue());
        gridSizeV = Utils.clamp(2, 1000, gridSizeV);
        sM.setUniformInt("uGridSize", (int) gridSizeV);
        sM.setUniformFloat("uTime", SWMain.getTotalTime());
        sM.setUniformFloat("uSpeed", speed.getValue());
        
        double octaveV = Math.ceil(octaves.getValue());
        octaveV = Utils.clamp(1, 10, octaveV);
        sM.setUniformInt("uOctaves", (int) octaveV);
        sM.setUniformFloat("uLacunarity", 2.0f);
        sM.setUniformFloat("uPersistence", 0.5f);

        sM.setUniformFloat("uGamma", gamma.getValue());
        sM.setUniformFloat("uGain", gain.getValue());

        sM.setUniformInt("uWarp", warp.getValue() ? 1 : 0);
        sM.setUniformFloat("uWarpStrength", warpStrength.getValue());
        sM.setUniformFloat("uWarpScale", warpScale.getValue());

        sM.setUniformInt("uOctaveRidge", octaveRidge.getValue() ? 1 : 0);
        sM.setUniformInt("uPostRidge", postRidge.getValue() ? 1 : 0);
        sM.setUniformFloat("uRidgePow", ridgePow.getValue());

        sM.setUniformInt("uOctaveTurbulence", octaveTurbulence.getValue() ? 1 : 0);
        sM.setUniformFloat("uTurbulencePow", turbulencePow.getValue());

        sM.setUniformVec3("uColorLow", colorLow);
        sM.setUniformVec3("uColorHigh", colorHigh);
        
        sM.bindVAO(quadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        
        // Reset
        glActiveTexture(GL_TEXTURE0);
        sM.bindVAO(0);
        sM.useProgram(0);
    }
    
    public Bindable<Float> getSpeed() {
        return speed;
    }
    
    public Bindable<Float> getGridSize() {
        return gridSize;
    }
    
    public Bindable<Float> getOctaves() {
        return octaves;
    }
    
    public Bindable<Float> getGamma() {
        return gamma;
    }
    
    public Bindable<Float> getGain() {
        return gain;
    }
    
    public Bindable<Boolean> getWarp() {
        return warp;
    }
    
    public Bindable<Float> getWarpStrength() {
        return warpStrength;
    }
    
    public Bindable<Float> getWarpScale() {
        return warpScale;
    }

    public Bindable<Boolean> getOctaveRidge() {
        return octaveRidge;
    }

    public Bindable<Boolean> getPostRidge() {
        return postRidge;
    }

    public Bindable<Float> getRidgePow() {
        return ridgePow;
    }

    public Bindable<Boolean> getOctaveTurbulence() {
        return octaveTurbulence;
    }

    public Bindable<Float> getTurbulencePow() {
        return turbulencePow;
    }
}
