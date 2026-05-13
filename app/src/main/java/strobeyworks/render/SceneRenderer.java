package strobeyworks.render;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.util.List;

import org.joml.Matrix4f;

import strobeyworks.SWMain;
import strobeyworks.object.Mesh;
import strobeyworks.object.Mesh.MeshType;
import strobeyworks.object.SceneObject;
import strobeyworks.platform.Animation;
import strobeyworks.platform.IOEvent;
import strobeyworks.platform.Renderer;
import strobeyworks.platform.ShaderManager;
import strobeyworks.render.lightsources.DirectionalLight;
import strobeyworks.render.lightsources.LightSource;
import strobeyworks.render.lightsources.SpotLight;
import strobeyworks.render.scenes.Scene;
import strobeyworks.render.scenes.WorkingScene;
import strobeyworks.utils.MeshStatics;
import strobeyworks.utils.Vec3;

public class SceneRenderer extends Renderer {
    
    private static SceneRenderer instance;

    private int gridProgram;
    private int objectProgram;
    private int shadowProgram;
    private int indicatorProgram;
    private Camera camera;
    private Scene scene;
    
    private int gridvAO;
    public Mesh indicatorSphere;
    
    private static final int SHADOW_WIDTH = 2048;
    private static final int SHADOW_HEIGHT = 2048;
    private static final int MAX_DIRECTIONAL_SHADOWS = 8;
    private static final int MAX_SPOT_SHADOWS = 8;
    private static final int MAX_DIRECTIONAL_LIGHTS = 8;
    private static final int MAX_SPOT_LIGHTS = 8;

    public static SceneRenderer getInstance() {
        if (instance==null) instance = new SceneRenderer();
        return instance;
    }
    
    private SceneRenderer() {
        camera = new Camera(this);
        scene = new WorkingScene();
        indicatorSphere = ObjLoader.loadMesh("sphere.obj", false, MeshType.SMOOTH_SHADED);
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
        camera.initialise();
        scene.initialise();

        camera.setPosition(new Vec3(0.4f, 5.9f, 6.8f));
        camera.setOrientation(-93f, -32f, 66f);

        ShaderManager sM = SWMain.getShaderManager();
        
        //Grid init
        gridProgram = sM.createProgram("grid.vert", "grid.frag");
        gridvAO = glGenVertexArrays();
        int vbo = glGenBuffers();
        sM.bindVAO(gridvAO);
        sM.bindVBO(vbo);
        glBufferData(GL_ARRAY_BUFFER, MeshStatics.GRID_VERTICES, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        sM.bindVAO(0);
        sM.bindVBO(0);
        
        //Object and shadow init
        objectProgram = sM.createProgram("screen.vert", "screen.frag");
        shadowProgram = sM.createProgram("shadow_depth.vert", "shadow_depth.frag");
        indicatorProgram = sM.createProgram("screen.vert", "light_indicator.frag");
        
        for (LightSource l : scene.getAllLights()) l.initShadowMap(SHADOW_WIDTH, SHADOW_HEIGHT);
        for (Mesh m : scene.getMeshes()) m.initBuffers();
        indicatorSphere.initBuffers();
        
        sM.useProgram(0);
    }

    @Override
    public void update() {
        String s = String.format("(%.1f, %.1f, %.1f) | Yaw %.1f | Pitch %.1f | FOV %.1f",
        camera.getPosition().x,
        camera.getPosition().y,
        camera.getPosition().z,
        camera.getYaw(),
        camera.getPitch(),
        camera.getFov());
        getParentWindow().setTitleData(s);

        camera.update(SWMain.getDeltaTime());
        scene.update();
    }
    
    @Override
    public void render() {
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        shadowPass();
        renderPass();
        indicatorPass();
    }
    
    private void indicatorPass() {
        ShaderManager sM = SWMain.getShaderManager();
        
        // Object rendering
        sM.useProgram(indicatorProgram);
        sM.setCurrentProgram(indicatorProgram);
        
        // View and projection uniforms
        sM.setUniformMat4("uView", camera.getViewMatrix());
        sM.setUniformMat4("uProjection", camera.getProjectionMatrix());
        sM.setUniformVec3("uViewPosition", camera.getPosition());
        
        for (SpotLight light : scene.getSpotLights()) {
            SceneObject indicator = light.getIndicator();
            sM.setUniformMat4("uModel", indicator.getModelMatrix());
            sM.setUniformVec3("uObjectColor", indicator.getColor());
            sM.setUniformFloat("uIntensity", light.getIntensity().getValue());
            sM.setUniformVec3("uColor", light.getColorVec3());
            
            Mesh m = indicator.getMesh();
            sM.bindVAO(m.getVAO());
            glDrawArrays(GL_TRIANGLES, 0, m.getVertexCount());
        }
    }
    
    private void shadowPass() {
        ShaderManager sM = SWMain.getShaderManager();
        
        //Directional shadow
        glViewport(0, 0, SHADOW_WIDTH, SHADOW_HEIGHT);
        sM.useProgram(shadowProgram);
        sM.setCurrentProgram(shadowProgram);
        
        int dirCount = Math.min(scene.getDirectionalLights().size(), MAX_DIRECTIONAL_SHADOWS);
        for (int i = 0; i < dirCount; i++) {
            DirectionalLight light = scene.getDirectionalLights().get(i);
            if (!light.shadowEnabled()||light.getIntensity().getValue()<0) continue;
            
            glBindFramebuffer(GL_FRAMEBUFFER, light.getShadowFBO());
            glClear(GL_DEPTH_BUFFER_BIT);
            
            sM.setUniformMat4("uLightSpaceMatrix", light.getLightSpaceMatrix());
            
            for (SceneObject o : scene.getObjects()) {
                sM.setUniformMat4("uModel", o.getModelMatrix());
                
                Mesh m = o.getMesh();
                sM.bindVAO(m.getVAO());
                glDrawArrays(GL_TRIANGLES, 0, m.getVertexCount());
            }
            
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
        
        //Spot shadow
        int spotCount = Math.min(scene.getSpotLights().size(), MAX_SPOT_SHADOWS);
        for (int i = 0; i < spotCount; i++) {
            SpotLight light = scene.getSpotLights().get(i);
            if (!light.shadowEnabled()||light.getIntensity().getValue()<0) continue;
            
            glBindFramebuffer(GL_FRAMEBUFFER, light.getShadowFBO());
            glClear(GL_DEPTH_BUFFER_BIT);
            
            sM.setUniformMat4("uLightSpaceMatrix", light.getLightSpaceMatrix());
            
            for (SceneObject o : scene.getObjects()) {
                sM.setUniformMat4("uModel", o.getModelMatrix());
                
                Mesh m = o.getMesh();
                sM.bindVAO(m.getVAO());
                glDrawArrays(GL_TRIANGLES, 0, m.getVertexCount());
            }
        }
        
        //Reset
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        sM.assertViewport(getParentWindow().getWidth(), getParentWindow().getHeight());
        
        sM.bindVAO(0);
        sM.useProgram(0);
        
    }
    
    private void renderPass() {
        ShaderManager sM = SWMain.getShaderManager();
        
        // Object rendering
        sM.useProgram(objectProgram);
        sM.setCurrentProgram(objectProgram);
        
        // View and projection uniforms
        sM.setUniformMat4("uView", camera.getViewMatrix());
        sM.setUniformMat4("uProjection", camera.getProjectionMatrix());
        sM.setUniformVec3("uViewPosition", camera.getPosition());
        
        // Phong uniforms
        sM.setUniformFloat("uAmbientStrength", 0.0f);
        sM.setUniformFloat("uSpecularStrength", 0.5f);
        sM.setUniformFloat("uShininess", 32.0f);
        
        // Directional light uniforms + shadow maps
        List<DirectionalLight> directionalLights = scene.getDirectionalLights();
        int directionalCount = Math.min(directionalLights.size(), MAX_DIRECTIONAL_LIGHTS);
        sM.setUniformInt("uDirectionalLightCount", directionalCount);
        int firstDirectionalShadowTextureUnit = 3;
        
        for (int i = 0; i < directionalCount; i++) {
            DirectionalLight light = directionalLights.get(i);
            
            sM.setUniformVec3("uDirectionalLights[" + i + "].direction", light.getDirection());
            sM.setUniformVec3("uDirectionalLights[" + i + "].color", light.getColorVec3());
            sM.setUniformFloat("uDirectionalLights[" + i + "].intensity", light.getIntensity().getValue());
            
            sM.setUniformMat4("uDirectionalLightSpaceMatrices[" + i + "]", light.getLightSpaceMatrix());
            sM.setUniformInt("uDirectionalShadowEnabled[" + i + "]", light.shadowEnabled() ? 1 : 0);
            
            int textureUnit = firstDirectionalShadowTextureUnit + i;
            glActiveTexture(GL_TEXTURE0 + textureUnit);
            glBindTexture(GL_TEXTURE_2D, light.getShadowDepthTexture());
            sM.setUniformInt("uDirectionalShadowMaps[" + i + "]", textureUnit);
        }
        
        // Spotlight uniforms
        List<SpotLight> spotLights = scene.getSpotLights();
        int spotCount = Math.min(spotLights.size(), MAX_SPOT_LIGHTS);
        sM.setUniformInt("uSpotLightCount", spotCount);
        int firstSpotShadowTextureUnit = 3 + MAX_DIRECTIONAL_SHADOWS;
        
        for (int i = 0; i < spotCount; i++) {
            SpotLight light = spotLights.get(i);
            
            sM.setUniformVec3("uSpotLights[" + i + "].position", light.getPosition());
            sM.setUniformVec3("uSpotLights[" + i + "].direction", light.getDirection());
            sM.setUniformVec3("uSpotLights[" + i + "].color", light.getColorVec3());
            sM.setUniformFloat("uSpotLights[" + i + "].intensity", light.getIntensity().getValue());
            
            sM.setUniformFloat("uSpotLights[" + i + "].innerCutoffCos", light.getInnerCutoffCos());
            sM.setUniformFloat("uSpotLights[" + i + "].outerCutoffCos", light.getOuterCutoffCos());
            
            sM.setUniformFloat("uSpotLights[" + i + "].constant", light.getConstant());
            sM.setUniformFloat("uSpotLights[" + i + "].linear", light.getLinear());
            sM.setUniformFloat("uSpotLights[" + i + "].quadratic", light.getQuadratic());
            
            sM.setUniformMat4("uSpotLightSpaceMatrices[" + i + "]", light.getLightSpaceMatrix());
            sM.setUniformInt("uSpotShadowEnabled[" + i + "]", light.shadowEnabled() ? 1 : 0);
            
            int textureUnit = firstSpotShadowTextureUnit + i;
            glActiveTexture(GL_TEXTURE0 + textureUnit);
            glBindTexture(GL_TEXTURE_2D, light.getShadowDepthTexture());
            sM.setUniformInt("uSpotShadowMaps[" + i + "]", textureUnit);
        }
        
        // Draw objects
        for (SceneObject o : scene.getObjects()) {
            sM.setUniformMat4("uModel", o.getModelMatrix());
            sM.setUniformVec3("uObjectColor", o.getColor());
            
            Mesh m = o.getMesh();
            sM.bindVAO(m.getVAO());
            glDrawArrays(GL_TRIANGLES, 0, m.getVertexCount());
        }
        
        // Grid rendering
        sM.useProgram(gridProgram);
        sM.setCurrentProgram(gridProgram);
        
        sM.setUniformMat4("uView", camera.getViewMatrix());
        sM.setUniformMat4("uProjection", camera.getProjectionMatrix());
        sM.setUniformMat4("uModel", new Matrix4f());
        
        sM.bindVAO(gridvAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        
        // Reset
        glActiveTexture(GL_TEXTURE0);
        sM.bindVAO(0);
        sM.useProgram(0);
    }

    public Scene getScene() {return this.scene;}
}
