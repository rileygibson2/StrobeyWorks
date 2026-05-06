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
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joml.Matrix4f;

import strobeyworks.SWMain;
import strobeyworks.logger.Logger;
import strobeyworks.platform.Animation;
import strobeyworks.platform.IOEvent;
import strobeyworks.platform.IOEvent.IOEventType;
import strobeyworks.platform.Renderer;
import strobeyworks.platform.ShaderManager;
import strobeyworks.render.SceneRenderer;
import strobeyworks.render.lightsources.LightSource;
import strobeyworks.render.scenes.Scene;
import strobeyworks.ui.components.UITab;
import strobeyworks.ui.components.interactable.UICheckBox;
import strobeyworks.ui.components.interactable.UISlider;
import strobeyworks.ui.components.interactable.input.UIFloatInputRule;
import strobeyworks.ui.components.interactable.input.UIStringInputRule;
import strobeyworks.ui.components.interactable.input.UIUserInput;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UIQuad;
import strobeyworks.ui.core.UITexture;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIElement.UIAlignContent;
import strobeyworks.ui.primitives.UIElement.UIAlignItems;
import strobeyworks.ui.primitives.UIElement.UIBoxMode;
import strobeyworks.ui.primitives.UIElement.UIFlowDirection;
import strobeyworks.ui.primitives.UIElement.UIPositionMode;
import strobeyworks.ui.primitives.UIIcon;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.utils.Vec4;

public class UIRenderer extends Renderer {
    
    private static UIRenderer instance;
    
    private int shapeProgram;
    private int textProgram;
    private int iconProgram;
    
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
    private UIElement root;
    private UIElement focussed;
    private UIElement pointer;
    
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
        font.loadFromTTF("RobotoMono-Medium.ttf", 30f);
        
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
        //.justifyContent(UIJustifyContent.CENTER)
        .alignItems(UIAlignItems.CENTER)
        .alignContent(UIAlignContent.CENTER)
        .flowDirection(UIFlowDirection.COLUMN)
        .flowWrap(false);
        
        addToRoot(pane2);

        UITexture tex = new UITexture("test.png");
        UIIcon icon = new UIIcon(sw(0.06f), sh(0.03f), tex);
        icon.tint(col(UIColors.GREEN));
        pane2.addChild(icon);
        
        UIFloatInputRule inputRule = new UIFloatInputRule();
        inputRule.maxCharacters(5)
        .maxPrecision(2)
        .inputMinMax(0f, 100f)
        .mappedMinMax(0f, 1f);
        
        UIUserInput<Float> input = new UIUserInput<>(sw(0.2f), sh(0.08f), font, inputRule);
        input.borderColor(col(UIColors.GREEN))
        .cornerRadius(10f)
        .marginTop(px(20));
        
        pane2.addChild(input);
        
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
        
        input.bindTo(spot.getIntensity());
        sliders.get(0).bindTo(spot.getIntensity());
        sliders.get(1).bindTo(spot.getRed());
        sliders.get(2).bindTo(spot.getGreen());
        sliders.get(3).bindTo(spot.getBlue());
        checkBox.bindTo(spot.getShadowEnabled());
        
        Animation a = new Animation(3, (i, value) -> {
            sliders.get(i+1).setLocalValue(value);
            sliders.get(i+1).commitLocalValue();
        });
        a.setWidth(1f);
        a.setSpeed(0.2f);
        //a.setPhase(0f, 0.2f);
        //addAnimation(a);
        
    }
    
    public void addToRoot(UIElement e) {
        root.addChild(e);
    }
    
    public void receiveIOEvent(IOEvent event) {
        if (root==null) return;
        
        switch (event.getEventType()) {
            case KEY_DOWN:
            case KEY_UP:
            case CHAR_TYPED:
            if (focussed!=null) focussed.handleIOEvent(event);
            return;
            
            case DRAG:
            case LEFT_RELEASE:
            if (pointer!=null) {
                pointer.handleIOEvent(event);
                
                if (event.getEventType()==IOEventType.LEFT_RELEASE) {
                    pointer.lostPointer(event);
                    pointer = null;
                }
            }
            return;
            
            case LEFT_PRESS:
            UIElement e = root.getDeepestElementAt(event.getMouseX(), event.getMouseY());
            UIElement target = e;
            
            while (target!=null&&!target.isInteractable()) { // Track up until found interactable element
                target = target.getParent();
            }
            
            boolean handled = false;
            
            if (focussed!=null&&focussed!=target) {
                focussed.lostFocus(event);
                focussed = null;
            }
            
            if (target!=null&&target.isFocussable()) {
                focussed = target;
                focussed.gotFocus(event);
                handled = true;
            }
            
            if (target!=null&&target.wantsPointer()) {
                pointer = target;
                pointer.gotPointer(event);
                handled = true;
            }
            
            if (target!=null&&!handled) target.handleIOEvent(event);
            
            default: break;
        }
    }
    
    @Override
    public void handleWindowResize() {
        buildProjectionMatrix();
        root.markLayoutDirty();
    }
    
    public void rebuildVisibleElementList() {
        visibleUIElements = root.getVisibleChildren();
        root.clearSubtreeDirtyMark();
    }
    
    public void initialise() {
        root = new UIRectangle(sw(1f), sh(1f));
        ((UIRectangle) root).color(col(UIColors.BLACK))
        .position(UIPositionMode.SCREEN)
        .box(UIBoxMode.FIXED)
        .flowDirection(UIFlowDirection.COLUMN);
        
        root.markLayoutDirty();
        root.markSubtreeDirty();
        
        ShaderManager sM = SWMain.getShaderManager();
        
        shapeProgram = sM.createProgram("ui/ui_shapes.vert", "ui/ui_shapes.frag");
        textProgram = sM.createProgram("ui/ui_text.vert", "ui/ui_text.frag");
        iconProgram = sM.createProgram("ui/ui_icon.vert", "ui/ui_icon.frag");
        
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
        
        if (root.isLayoutDirty()) {
            root.layoutMeasure();
            root.layoutAdvance(root.getMeasuredX(), root.getMeasuredY());
        }
        
        if (root.isSubtreeDirty()) {
            rebuildVisibleElementList();
            root.clearSubtreeDirtyMark();
        }
        
        if (root.subtreeNeedsInitialising()) {
            root.initialiseSubtree();
            if (root.isLayoutDirty()) {
                root.layoutMeasure();
                root.layoutAdvance(root.getMeasuredX(), root.getMeasuredY());
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
            else if (e instanceof UIIcon) renderIcon(sM, (UIIcon) e);
            else renderShape(sM, e);
        }
        
        // Reset
        glActiveTexture(GL_TEXTURE0);
        sM.bindVAO(0);
        sM.useProgram(0);
    }
    
    private void renderText(ShaderManager sM, UIText tE) {
        UIFont font = tE.getFont();
        float baselineY = tE.getResolvedY() + font.getAscent();
        
        
        float[] vertices = font.buildTextVertices(
            tE.getText(),
            tE.getResolvedX(),
            baselineY
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
    
    private void renderIcon(ShaderManager sM, UIIcon icon) {
        sM.useProgram(iconProgram);
        sM.setCurrentProgram(iconProgram);
        
        sM.setUniformMat4("uProjection", projectionMatrix);
        sM.setUniformMat4("uModel", icon.getModelMatrix());
        sM.setUniformVec4("uTint", icon.getTint());
        sM.setUniformVec4("uUVRect", icon.getUVRect());
        
        glActiveTexture(GL_TEXTURE0);
        sM.bindTexture(icon.getTextureId());
        sM.setUniformInt("uTexture", 0);
        
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
