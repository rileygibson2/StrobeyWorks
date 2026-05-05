package strobeyworks.ui;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
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
import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UIPair.pcw;
import static strobeyworks.ui.core.UIPair.px;
import static strobeyworks.ui.core.UIPair.sh;
import static strobeyworks.ui.core.UIPair.sw;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;


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
import strobeyworks.render.SceneRenderer;
import strobeyworks.render.lightsources.LightSource;
import strobeyworks.render.scenes.Scene;
import strobeyworks.ui.components.UICheckBox;
import strobeyworks.ui.components.UISlider;
import strobeyworks.ui.components.UITab;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UIQuad;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIElement.UIAlignItems;
import strobeyworks.ui.primitives.UIElement.UIBoxMode;
import strobeyworks.ui.primitives.UIElement.UIFlowDirection;
import strobeyworks.ui.primitives.UIElement.UIJustifyContent;
import strobeyworks.ui.primitives.UIElement.UIPositionMode;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.utils.Vec4;

public class UIRenderer extends Renderer {
    
    private static UIRenderer instance;
    
    private int shapeProgram;
    private int textProgram;
    
    private Matrix4f projectionMatrix;
    
    private float[] quadVertices = {
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f,
        0.5f,  0.5f, 0.0f,
        0.5f,  0.5f, 0.0f,
        -0.5f,  0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f
    };
    private int quadVAO;
    
    private int textVAO;
    private int textVBO;
    
    private List<UIElement> visibleUIElements;
    private UIElement rootUIElement;
    
    protected Set<Animation> animations;
    
    public static UIRenderer getInstance() {
        if (instance==null) instance = new UIRenderer();
        return instance;
    }
    
    private UIRenderer() {
        visibleUIElements = new ArrayList<>();
        animations = new HashSet<Animation>();
    }
    
    private void buildTest() {
        UIFont font = new UIFont();
        font.loadFromTTF("RobotoMono-Medium.ttf", 20f);
        
        UITab tab = new UITab(pcw(1f), sh(0.1f), 5);
        tab.marginTop(px(2));
        addToRoot(tab);
        
        UIRectangle pane2 = new UIRectangle(sw(1f), sh(0.89f));
        pane2.color(col(UIColors.GRAY_008))
        .cornerRadius(new Vec4(0f, 0f, 20f, 20f))
        .borderColor(col(UIColors.GREEN))
        .borderThickness(1.5f)
        .borderTop(false)
        .padding(new UIQuad(px(5)))
        .justifyContent(UIJustifyContent.CENTER)
        .alignItems(UIAlignItems.CENTER)
        //.alignContent(UIAlignContent.CENTER)
        .flowDirection(UIFlowDirection.COLUMN)
        .flowWrap(false);
        
        addToRoot(pane2);

        UIText text = new UIText(sw(0.3f), sh(0.3f), font, "Hello");
        text.color(col(UIColors.GREEN));
        text.enableDebugColor(true);
        pane2.addChild(text);
        
        List<UISlider> sliders = new ArrayList<>();
        int num = 4;
        for (int i=0; i<num; i++) {
            UISlider slider = new UISlider(sw(0.9f), sh(0.08f));
            slider.marginTop(px(10));
            pane2.addChild(slider);
            sliders.add(slider);
        }
        
        UICheckBox checkBox = new UICheckBox(sw(0.1f), sw(0.1f), true);
        checkBox.marginTop(px(10));
        pane2.addChild(checkBox);
        
        Scene scene = SceneRenderer.getInstance().getScene();
        LightSource spot = scene.getSpotLights().get(0);
        
        sliders.get(0).bindTo(spot.getIntensity());
        sliders.get(1).bindTo(spot.getRed());
        sliders.get(2).bindTo(spot.getGreen());
        sliders.get(3).bindTo(spot.getBlue());
        checkBox.bindTo(spot.getShadowEnabled());
        
        Animation a = new Animation(3, (i, value) -> {
            sliders.get(i+1).setValue(value);
        });
        a.setWidth(1f);
        a.setSpeed(0.2f);
        //a.setPhase(0f, 0.2f);
        //addAnimation(a);
        
    }
    
    public void addToRoot(UIElement e) {
        rootUIElement.addChild(e);
    }
    
    public void handleIOEvent(IOEvent event) {
        if (rootUIElement==null) return;
        rootUIElement.eventTraverse(event);
    }
    
    @Override
    public void handleWindowResize() {
        buildProjectionMatrix();
        rootUIElement.markLayoutDirty();
    }
    
    public void rebuildVisibleElementList() {
        visibleUIElements = rootUIElement.getVisibleChildren();
        rootUIElement.clearSubtreeDirtyMark();
    }
    
    public void initialise() {
        rootUIElement = new UIRectangle(sw(1f), sh(1f));
        ((UIRectangle) rootUIElement).color(col(UIColors.BLACK))
        .position(UIPositionMode.SCREEN)
        .box(UIBoxMode.FIXED)
        .flowDirection(UIFlowDirection.COLUMN);
        
        rootUIElement.markLayoutDirty();
        rootUIElement.markSubtreeDirty();
        
        ShaderManager sM = SWMain.getShaderManager();
        
        shapeProgram = sM.createProgram("ui/ui_shapes.vert", "ui/ui_shapes.frag");
        textProgram = sM.createProgram("ui/ui_text.vert", "ui/ui_text.frag");
        
        // Shape setup
        quadVAO = glGenVertexArrays();
        int quadVBO = glGenBuffers();
        sM.bindVAO(quadVAO);
        sM.bindVBO(quadVBO);
        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        sM.bindVAO(0);
        sM.bindVBO(0);
        
        // Text setup
        textVAO = glGenVertexArrays();
        textVBO = glGenBuffers();
        
        sM.bindVAO(textVAO);
        sM.bindVBO(textVBO);
        
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);
        
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
        
        if (rootUIElement.isSubtreeDirty()) {
            rebuildVisibleElementList();
            rootUIElement.clearSubtreeDirtyMark();
        }
        
        if (rootUIElement.subtreeNeedsInitialising()) {
            rootUIElement.initialiseSubtree();
            if (rootUIElement.isLayoutDirty()) {
                rootUIElement.layoutMeasure();
                rootUIElement.layoutAdvance(rootUIElement.getMeasuredX(), rootUIElement.getMeasuredY());
            }
        }
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);
        
        ShaderManager sM = SWMain.getShaderManager();
        
        // Draw objects
        for (UIElement e : visibleUIElements) {
            if (e instanceof UIText) renderText(sM, (UIText) e);
            else renderShape(sM, e);
        }
        
        // Reset
        glActiveTexture(GL_TEXTURE0);
        sM.bindVAO(0);
        sM.useProgram(0);
    }
    
    private void renderText(ShaderManager sM, UIText tE) {
        UIFont font = tE.getFont();
        
        float[] vertices = font.buildTextVertices(
            tE.getText(),
            tE.getResolvedX(),
            tE.getResolvedY()
        );
        
        sM.useProgram(textProgram);
        sM.setCurrentProgram(textProgram);
        
        sM.setUniformMat4("uProjection", projectionMatrix);
        tE.setRenderUniforms(sM);
        
        glActiveTexture(GL_TEXTURE0);
        sM.bindTexture(font.getTextureId());
        sM.setUniformInt("uFontAtlas", 0);
        
        sM.bindVAO(textVAO);
        sM.bindVBO(textVBO);
        
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);
        
        glDrawArrays(GL_TRIANGLES, 0, vertices.length / 4);
        
    }
    
    private void renderShape(ShaderManager sM, UIElement e) {
        sM.useProgram(shapeProgram);
        sM.setCurrentProgram(shapeProgram);

        sM.setUniformMat4("uProjection", projectionMatrix);
        sM.setUniformMat4("uModel", e.getModelMatrix());
        e.setRenderUniforms(sM);

        sM.bindVAO(quadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
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
