package strobeyworks.ui;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joml.Matrix4f;

import strobeyworks.Animation;
import strobeyworks.SWMain;
import strobeyworks.ShaderManager;
import strobeyworks.logger.Logger;
import strobeyworks.render.Renderer;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIQuad;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIElement.UIBoxMode;
import strobeyworks.ui.primitives.UIElement.UIPosMode;
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
    
    private List<UIElement> visibleUIElements;
    private UIElement rootUIElement;
    
    protected Set<Animation> animations;
    
    public UIRenderer() {
        visibleUIElements = new ArrayList<>();
        animations = new HashSet<Animation>();
    }
    
    private void buildTest() {
        UIPane pane1 = new UIPane(sw(0f), sh(0f), pw(1f), ph(0.2f));
        pane1.setColor(new Vec3(0.4f, 0.4f, 0.4f));
        pane1.setCornerRadius(new Vec4(20f, 20f, 0f, 0f));
        pane1.setPadding(new UIQuad(px(10)));

        pane1.setBoxMode(UIBoxMode.FLEX);
        pane1.setMinWidth(sw(0.6f));

        rootUIElement.addChild(pane1);
        
        int num = 3;
        UIRectangle r = new UIRectangle(sw(0f), sh(0f), sw(0.1f), sh(0.5f));

        for (int i=0; i<num; i++) {
            UIRectangle rect = new UIRectangle(sw(0f), sh(0f), sw(0.1f), sh(0.5f));
            if (i==1) rect = r;
            rect.setColor(new Vec3(0f, 0f, 1f));
            rect.setCornerRadius(new Vec4(20f));
            pane1.addChild(rect);
        }
        
        Animation a = new Animation(1, (i, value) -> {
            r.setX(sw(value));
        });
        a.setSpeed(0.4f);
        animations.add(a);

        
    }
    
    public void rebuildVisibleElementList() {
        visibleUIElements = rootUIElement.getVisibleChildren();
        rootUIElement.clearSubtreeDirtyMark();
        
        int i = 0;
        for (UIElement e : visibleUIElements) {
            Logger.debug(i+": "+e.getClass());
            i++;
        }
    }
    
    public void init() {
        rootUIElement = new UIRectangle(px(0), px(0), sw(1f), sh(1f));
        rootUIElement.setPositionMode(UIPosMode.SCREEN_ABSOLUTE);
        rootUIElement.setBoxMode(UIBoxMode.FIXED);

        ((UIRectangle) rootUIElement).setColor(new Vec3(0.3f));

        rootUIElement.markLayoutDirty();
        rootUIElement.markSubtreeDirty();

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
    }
    
    public void render() {
        // Updates
        for (Animation a : animations) a.trigger();

        if (rootUIElement.isLayoutDirty()) {
            rootUIElement.layoutMeasure();
            rootUIElement.layoutAdvance(rootUIElement.getMeasuredX(), rootUIElement.getMeasuredY());
        }

        if (rootUIElement.isSubtreeDirty()) rebuildVisibleElementList();
        
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);
        
        ShaderManager sM = SWMain.getShaderManager();
        sM.useProgram(uiProgram);
        sM.setCurrentProgram(uiProgram);
        sM.setUniformMat4("uProjection", projectionMatrix);
        
        // Draw objects
        for (UIElement e : visibleUIElements) {
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
