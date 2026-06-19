package strobeyworks.rendernodes;

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
import strobeyworks.nodes.RenderNode;
import strobeyworks.nodes.RenderTarget;
import strobeyworks.platform.ShaderManager;
import strobeyworks.utils.BindableValue;
import strobeyworks.utils.Utils;
import strobeyworks.utils.Vec3;

public class PerlinNode extends RenderNode {
    
    private static PerlinNode instance;
    
    private int noiseProgram;
    private int quadVAO;
    
    private BindableValue<Float> speed;
    private BindableValue<Float> gridSize;

    private BindableValue<Float> octaves;

    private BindableValue<Float> gamma;
    private BindableValue<Float> gain;

    private BindableValue<Boolean> warp;
    private BindableValue<Float> warpStrength;
    private BindableValue<Float> warpScale;

    private BindableValue<Boolean> octaveRidge;
    private BindableValue<Boolean> postRidge;
    private BindableValue<Float> ridgePow;

    private BindableValue<Boolean> octaveTurbulence;
    private BindableValue<Float> turbulencePow;

    private Vec3 colorLow;
    private Vec3 colorHigh;
    
    public static PerlinNode getInstance() {
        if (instance==null) instance = new PerlinNode();
        return instance;
    }
    
    private PerlinNode() {
        this.speed = BindableValue.of(0.5f);
        this.gridSize = BindableValue.of(10f);
        this.octaves = BindableValue.of(1f);
        this.gamma = BindableValue.of(1f);
        this.gain = BindableValue.of(1f);

        this.warp = BindableValue.of(false);
        this.warpStrength = BindableValue.of(0f);
        this.warpScale = BindableValue.of(1f);

        this.octaveRidge = BindableValue.of(false);
        this.postRidge = BindableValue.of(false);
        this.ridgePow = BindableValue.of(2f);

        this.octaveTurbulence = BindableValue.of(false);
        this.turbulencePow = BindableValue.of(2f);

        this.colorLow = new Vec3(0f, 0f, 0f);
        this.colorHigh = new Vec3(1f, 0f, 0.8f);
    }

    @Override
    protected void setupControls() {}
    
    @Override
    public void initialise(int outputWidth, int outputHeight) {
        super.initialise(outputWidth, outputHeight);
        
        ShaderManager sM = ShaderManager.getInstance();
        
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
        ShaderManager sM = ShaderManager.getInstance();
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
    
    public BindableValue<Float> getSpeed() {
        return speed;
    }
    
    public BindableValue<Float> getGridSize() {
        return gridSize;
    }
    
    public BindableValue<Float> getOctaves() {
        return octaves;
    }
    
    public BindableValue<Float> getGamma() {
        return gamma;
    }
    
    public BindableValue<Float> getGain() {
        return gain;
    }
    
    public BindableValue<Boolean> getWarp() {
        return warp;
    }
    
    public BindableValue<Float> getWarpStrength() {
        return warpStrength;
    }
    
    public BindableValue<Float> getWarpScale() {
        return warpScale;
    }

    public BindableValue<Boolean> getOctaveRidge() {
        return octaveRidge;
    }

    public BindableValue<Boolean> getPostRidge() {
        return postRidge;
    }

    public BindableValue<Float> getRidgePow() {
        return ridgePow;
    }

    public BindableValue<Boolean> getOctaveTurbulence() {
        return octaveTurbulence;
    }

    public BindableValue<Float> getTurbulencePow() {
        return turbulencePow;
    }

    @Override
    protected void handleOutputResize() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleOutputResize'");
    }

    @Override
    protected void handleCleanup() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleCleanup'");
    }
}
