package strobeyworks.ui.core;

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
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.px;
import static strobeyworks.ui.core.UILength.sh;
import static strobeyworks.ui.core.UILength.sw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Matrix4f;

import strobeyworks.SWMain;
import strobeyworks.platform.Animation;
import strobeyworks.platform.IOEvent;
import strobeyworks.platform.Renderer;
import strobeyworks.platform.ShaderManager;
import strobeyworks.platform.Transition;
import strobeyworks.render.SceneRenderer;
import strobeyworks.render.lightsources.LightSource;
import strobeyworks.render.scenes.Scene;
import strobeyworks.ui.components.UITab;
import strobeyworks.ui.components.input.UICheckBox;
import strobeyworks.ui.components.input.UISlider;
import strobeyworks.ui.components.input.field.UIFieldRule;
import strobeyworks.ui.components.input.field.UIFloatField;
import strobeyworks.ui.components.input.field.UIFloatFieldRule;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIElement.UIAlignContent;
import strobeyworks.ui.primitives.UIElement.UIAlignItems;
import strobeyworks.ui.primitives.UIElement.UIBoxMode;
import strobeyworks.ui.primitives.UIElement.UIFlowDirection;
import strobeyworks.ui.primitives.UIElement.UIJustifyContent;
import strobeyworks.ui.primitives.UIElement.UIOverflowMode;
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
    private UIElement rootElement;
    private UIElement focussedElement;
    private UIElement pointerElement;
    private UIElement hoveredElement;
    
    protected Set<Animation> animations;
    protected Map<UIElement, Transition> transitions;
    
    public static UIRenderer getInstance() {
        if (instance==null) instance = new UIRenderer();
        return instance;
    }
    
    private UIRenderer() {
        visibleUIElements = new ArrayList<>();
        animations = new HashSet<>();
        transitions = new HashMap<>();
    }
    
    private UIRectangle buildTestBase() {
        UIFontManager.loadFont("RobotoMono-Medium.ttf", 30f);
        UITextureManager.loadTexture("up_arrow.png");
        UITextureManager.loadTexture("down_arrow.png");
        
        UITab tab = new UITab(pcw(1f), sh(0.1f), 5);
        tab.style("margin-top", px(2));
        addToRoot(tab);
        
        UIRectangle pane2 = new UIRectangle();
        pane2.style("width", sw(1f))
        .style("height", sh(0.89f))
        .style("color", col(UIColors.GRAY_008))
        .style("corner-radius", new Vec4(0f, 0f, 20f, 20f))
        .style("border-color", col(UIColors.GREEN))
        .style("border-thickness", px(2))
        .style("border-top", false)
        .style("border-enabled", true)
        .style("padding-left", px(5))
        .style("padding-right", px(5))
        .style("padding-top", px(5))
        .style("padding-bottom", px(5))
        //.justifyContent(UIJustifyContent.CENTER)
        .style("align-items", UIAlignItems.CENTER)
        .style("align-content", UIAlignContent.CENTER)
        .style("flow-direction", UIFlowDirection.COLUMN);
        
        addToRoot(pane2);
        return pane2;
    }
    
    public void buildTest1(UIRectangle pane) {
        UIFont font = UIFontManager.getUIFont("RobotoMono-Medium.ttf", 30f);
        
        UIFloatFieldRule inputRule = UIFieldRule.defaultFloat();
        inputRule.maxCharacters(3)
        .maxPrecision(0)
        .inputMinMax(0f, 100f)
        .mappedMinMax(0f, 1f);
        
        UIFloatField field = new UIFloatField(sw(0.2f), sh(0.08f), font, inputRule);
        field.useButtons(0.1f);
        field.style("margin-top", px(20));
        
        pane.addChild(field);
        
        List<UISlider> sliders = new ArrayList<>();
        int num = 4;
        for (int i=0; i<num; i++) {
            UISlider slider = new UISlider(sw(0.9f), sh(0.08f));
            slider.style("margin-top", px(10));
            pane.addChild(slider);
            sliders.add(slider);
        }
        
        UICheckBox checkBox = new UICheckBox(sw(0.1f), sw(0.1f), true);
        checkBox.style("margin-top", px(10));
        pane.addChild(checkBox);
        
        Scene scene = SceneRenderer.getInstance().getScene();
        LightSource spot = scene.getSpotLights().get(0);
        
        field.bindTo(spot.getIntensity());
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
    
    public void buildTest2(UIRectangle pane) {
        UIRectangle bounds = new UIRectangle();
        bounds.style("width", pcw(0.5f))
        .style("height", pch(0.5f))
        .style("border-color", col(UIColors.LAV))
        .style("border-enabled", true)
        .style("border-thickness", px(10f))
        .style("margin-top", px(20))
        .style("overflow", UIOverflowMode.HIDDEN);
        pane.addChild(bounds);
        
        for (int i=0; i<10; i++) {
            UIRectangle bC = new UIRectangle();
            bC.style("width", px(40))
            .style("height", px(50))
            .style("color", col(UIColors.TRANSPARENT))
            .style("margin-left", px(10))
            .style("margin-top", px(10))
            .style("justify-content", UIJustifyContent.CENTER)
            .style("align-items", UIAlignItems.CENTER)
            .style("border-enabled", true);
            bounds.addChild(bC);

            UIRectangle bC1 = new UIRectangle();
            bC1.style("width", pcw(0.5f))
            .style("height", pch(0.5f))
            .style("color", col(UIColors.RED))
            .style("border-enabled", true);
            bC.addChild(bC1);
        }
    }
    
    public void addToRoot(UIElement e) {
        rootElement.addChild(e);
    }
    
    @Override
    public void addAnimation(Animation a) {
        animations.add(a);
    }
    
    @Override
    public void removeAnimation(Animation a) {
        animations.remove(a);
    }
    
    public void addTransition(UIElement e, Transition t) {
        if (transitions.containsKey(e)) transitions.get(e).interrupt();
        transitions.put(e, t);
    }
    
    @Override
    public void handleWindowResize() {
        buildProjectionMatrix();
        rootElement.markLayoutDirty();
    }
    
    @Override
    public void initialise() {
        rootElement = new UIRectangle();
        rootElement.style("width", sw(1f))
        .style("height", sh(1f))
        .style("color", col(UIColors.BLACK))
        .style("position", UIPositionMode.SCREEN)
        .style("box", UIBoxMode.FIXED)
        .style("flow-direction", UIFlowDirection.COLUMN);
        
        rootElement.markLayoutDirty();
        rootElement.markSubtreeDirty();
        
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
        
        UIRectangle pane = buildTestBase();
        buildTest1(pane);
    }
    
    @Override
    public void receiveIOEvent(IOEvent event) {
        if (rootElement==null) return;
        
        switch (event.getEventType()) {
            case KEY_DOWN:
            case KEY_UP:
            case CHAR_TYPED:
            if (focussedElement!=null) focussedElement.handleIOEvent(event);
            return;
            
            case DRAG:
            if (pointerElement!=null) pointerElement.handleIOEvent(event);
            return;
            
            case LEFT_RELEASE:
            if (pointerElement!=null) {
                pointerElement.lostPointer(event);
                pointerElement = null;
            }
            return;
            
            case MOUSE_MOVE:
            if (pointerElement!=null) return; // Suppress drag while element holds pointer
            
            UIElement hit = rootElement.getDeepestElementAt(event.getMouseX(), event.getMouseY());
            if (hit==null) return;
            
            UIElement target = hit.findAncestorMatching(UIElement::isHoverable);
            
            if (hoveredElement!=null&&hoveredElement!=target) {
                hoveredElement.lostHover(event);
                hoveredElement = null;
            }
            
            if (target!=null&&target.isHoverable()&&target!=hoveredElement) {
                hoveredElement = target;
                hoveredElement.gotHover(event);
            }
            return;
            
            case LEFT_PRESS:
            hit = rootElement.getDeepestElementAt(event.getMouseX(), event.getMouseY());
            if (hit==null) return;
            
            target = hit.findAncestorMatching(e -> e.isClickable() || e.isFocussable() || e.wantsPointer());
            
            boolean handled = false;
            
            if (focussedElement!=null&&focussedElement!=target) {
                focussedElement.lostFocus(event);
                focussedElement = null;
            }
            
            if (target!=null) {
                if (target.isClickable()) {
                    target.clicked(event);
                    handled = true;
                }
                
                if (target.isFocussable()) {
                    focussedElement = target;
                    focussedElement.gotFocus(event);
                    handled = true;
                }
                
                if (target.wantsPointer()) {
                    pointerElement = target;
                    pointerElement.gotPointer(event);
                    handled = true;
                }
                
                if (!handled) target.handleIOEvent(event);
            }
            return;
            
            default: break;
        }
    }
    
    public void buildVisibleElementList() {
        visibleUIElements = rootElement.getVisibleChildren();
        rootElement.clearSubtreeDirtyMark();
    }
    
    private void layout() {
        rootElement.layoutCalculate();
        
        UIBounds rootBounds = new UIBounds(
            0f,
            0f,
            getParentWindow().getWidth(),
            getParentWindow().getHeight()
        );
        
        rootElement.layoutPlace(0f, 0f, rootBounds);
    }
    
    @Override
    public void update() {
        for (Animation a : animations) a.trigger();
        
        for (Map.Entry<UIElement, Transition> entry : transitions.entrySet()) entry.getValue().update();
        transitions.entrySet().removeIf(entry -> entry.getValue().isComplete());
        
        if (rootElement.isLayoutDirty()) layout();
        
        if (rootElement.isSubtreeDirty()) {
            rootElement.initialiseSubtree();
            
            // Re-layout incase initalising caused a layout change
            if (rootElement.isLayoutDirty()) layout();
            
            buildVisibleElementList();
            rootElement.clearSubtreeDirtyMark();
        }
    }
    
    @Override
    public void render() {
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
        float baselineY = tE.getScreenY() + font.getAscent();
        
        float[] vertices = font.buildTextVertices(
            tE.getText(),
            tE.getScreenX(),
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
        
        icon.setRenderUniforms(sM);
        
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
