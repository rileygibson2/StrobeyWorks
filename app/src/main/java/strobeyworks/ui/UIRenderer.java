package strobeyworks.ui;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static strobeyworks.ui.primitives.UIPair.ph;
import static strobeyworks.ui.primitives.UIPair.pw;
import static strobeyworks.ui.primitives.UIPair.px;
import static strobeyworks.ui.primitives.UIPair.sh;
import static strobeyworks.ui.primitives.UIPair.sw;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import strobeyworks.SWMain;
import strobeyworks.ShaderManager;
import strobeyworks.render.Renderer;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.utils.Vec3;
import strobeyworks.utils.Vec4;

public class UIRenderer extends Renderer {
    
    private int uiProgram;
    private Matrix4f projectionMatrix;
    
    float[] quadVertices = {
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f,
        0.5f,  0.5f, 0.0f,
        
        0.5f,  0.5f, 0.0f,
        -0.5f,  0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f
    };
    
    private int quadVAO;
    
    private List<UIElement> allUIElements;
    private UIPane rootUIElement;
    
    public UIRenderer() {
        allUIElements = new ArrayList<>();
    }
    
    private void buildTest() {
        int w = getParentWindow().getWidth();
        int h = getParentWindow().getHeight();
        rootUIElement = new UIPane(px(0), px(0), px(w), px(h));
        rootUIElement.setColor(new Vec3(0.1f));

        UIPane pane1 = new UIPane(sw(0.2f), sh(0.2f), sw(0.1f), sh(0.1f));
        pane1.setColor(new Vec3(1f, 0f, 0f));
        rootUIElement.addElement(pane1);

        UIPane pane2 = new UIPane(pw(0.4f), ph(0.2f), sw(0.1f), sh(0.1f));
        pane2.setColor(new Vec3(0f, 0f, 1f));
        pane2.setCornerRadius(new Vec4(20f));
        rootUIElement.addElement(pane2);
    }
    
    public void rebuildElementList() {
        allUIElements = rootUIElement.getElements();
    }
    
    public void init() {
        ShaderManager sM = SWMain.getShaderManager();
        
        //Grid init
        uiProgram = sM.createProgram("ui/ui_elements.vert", "ui/ui_elements.frag");
        quadVAO = glGenVertexArrays();
        int quadVBO = glGenBuffers();
        sM.bindVAO(quadVAO);
        sM.bindVBO(quadVBO);
        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        sM.bindVAO(0);
        sM.bindVBO(0);
        sM.useProgram(0);
        
        buildProjectionMatrix();
        buildTest();

        rebuildElementList();
    }
    
    public void render() {
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        ShaderManager sM = SWMain.getShaderManager();
        sM.useProgram(uiProgram);
        sM.setCurrentProgram(uiProgram);
        sM.setUniformMat4("uProjection", projectionMatrix);
        
        // Draw objects
        for (UIElement e : allUIElements) {
            if (!e.isVisible()) continue;
            
            sM.setUniformMat4("uModel", e.getModelMatrix());
            e.setRenderUniforms(sM);
            sM.bindVAO(quadVAO);
            glDrawArrays(GL_TRIANGLES, 0, 6);
        }
        
        // Reset
        glActiveTexture(GL_TEXTURE0);
        sM.bindVAO(0);
        sM.useProgram(0);
    }
    
    private void buildProjectionMatrix() {
        projectionMatrix = new Matrix4f().ortho(
            0.0f,
            getParentWindow().getWidth(),
            getParentWindow().getHeight(),
            0.0f,
            -1.0f,
            1.0f
        );
    }
}
