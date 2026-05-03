package strobeyworks.ui;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
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
import static strobeyworks.ui.primitives.UIPair.pch;
import static strobeyworks.ui.primitives.UIPair.pcw;
import static strobeyworks.ui.primitives.UIPair.px;
import static strobeyworks.ui.primitives.UIPair.sh;
import static strobeyworks.ui.primitives.UIPair.sw;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joml.Matrix4f;

import strobeyworks.SWMain;
import strobeyworks.platform.Animation;
import strobeyworks.platform.IOEvent;
import strobeyworks.platform.Renderer;
import strobeyworks.platform.ShaderManager;
import strobeyworks.platform.Animation.AnimationForm;
import strobeyworks.ui.components.UISlider;
import strobeyworks.ui.components.UITab;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIElement.UIAlignContent;
import strobeyworks.ui.primitives.UIElement.UIAlignItems;
import strobeyworks.ui.primitives.UIElement.UIBoxMode;
import strobeyworks.ui.primitives.UIElement.UIFlowDirection;
import strobeyworks.ui.primitives.UIElement.UIJustifyContent;
import strobeyworks.ui.primitives.UIElement.UIPositionMode;
import strobeyworks.ui.primitives.UIQuad;
import strobeyworks.ui.primitives.UIRectangle;
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
        UITab tab = new UITab(pcw(1f), sh(0.1f), 5);
        tab.marginTop(px(2));
        addToRoot(tab);
        
        UIRectangle pane2 = new UIRectangle(sw(1f), sh(0.89f));
        pane2.color(UIColors.color(UIColors.GRAY_01))
        .cornerRadius(new Vec4(0f, 0f, 20f, 20f))
        .borderColor(UIColors.color(UIColors.GREEN))
        .borderThickness(1.5f)
        .borderTop(false)
        .padding(new UIQuad(px(5)))
        .justifyContent(UIJustifyContent.CENTER)
        .alignItems(UIAlignItems.CENTER)
        .alignContent(UIAlignContent.CENTER)
        .flowDirection(UIFlowDirection.COLUMN)
        .flowWrap(false);
        
        addToRoot(pane2);
        
        List<UISlider> sliders = new ArrayList<>();
        int num = 8;
        for (int i=0; i<num; i++) {
            UISlider slider = new UISlider(sw(0.9f), sh(0.08f));
            slider.marginTop(px(10));
            pane2.addChild(slider);
            sliders.add(slider);
        }
        
        Animation a = new Animation(num, (i, value) -> {
            sliders.get(i).setValue(value);
        });
        a.setWidth(0.5f);
        a.setSpeed(0.2f);
        a.setPhase(0f, 0.2f);
        SWMain.getUIWindow().getRenderer().addAnimation(a);
        
    }
    
    public void addToRoot(UIElement e) {
        rootUIElement.addChild(e);
    }
    
    public void rebuildVisibleElementList() {
        visibleUIElements = rootUIElement.getVisibleChildren();
        rootUIElement.clearSubtreeDirtyMark();
    }
    
    public void handleIOEvent(IOEvent event) {
        if (rootUIElement==null) return;
        rootUIElement.eventTraverse(event);
    }
    
    public void init() {
        rootUIElement = new UIRectangle(sw(1f), sh(1f));
        ((UIRectangle) rootUIElement).color(UIColors.color(UIColors.BLACK))
        .position(UIPositionMode.SCREEN)
        .box(UIBoxMode.FIXED)
        .flowDirection(UIFlowDirection.COLUMN);
        
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
    
    public void addAnimation(Animation a) {
        animations.add(a);
    }
    
    public void removeAnimation(Animation a) {
        animations.remove(a);
    }
    
    public void render() {
        // Updates
        for (Animation a : animations) a.trigger();
        
        if (rootUIElement.isLayoutDirty()) {
            rootUIElement.layoutMeasure();
            rootUIElement.layoutAdvance(rootUIElement.getMeasuredX(), rootUIElement.getMeasuredY());
        }
        if (rootUIElement.isSubtreeDirty()) rebuildVisibleElementList();
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

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
