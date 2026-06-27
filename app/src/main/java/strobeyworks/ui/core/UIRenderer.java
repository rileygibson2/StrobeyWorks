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
import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
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

import strobeyworks.platform.Animation;
import strobeyworks.platform.IOEvent;
import strobeyworks.platform.ShaderManager;
import strobeyworks.platform.Transition;
import strobeyworks.platform.WindowRenderer;
import strobeyworks.ui.components.UIColorPicker;
import strobeyworks.ui.components.input.notifications.UIBanner;
import strobeyworks.ui.components.input.notifications.UIBanner.UIBannerMode;
import strobeyworks.ui.components.popups.UIPopup;
import strobeyworks.ui.logicpages.UIInspectorPane;
import strobeyworks.ui.logicpages.UIA.UIAArea;
import strobeyworks.ui.primitives.UIConnection;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIElement.UIBoxMode;
import strobeyworks.ui.primitives.UIElement.UIFlowDirection;
import strobeyworks.ui.primitives.UIElement.UIOverflowMode;
import strobeyworks.ui.primitives.UIElement.UIPositionMode;
import strobeyworks.ui.primitives.UIIcon;
import strobeyworks.ui.primitives.UIRectFactory;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.ui.primitives.UITextureView;
import strobeyworks.utils.Vec4;

public class UIRenderer extends WindowRenderer {
    
    private static UIRenderer instance;
    
    private int shapeProgram;
    private int textProgram;
    private int iconProgram;
    private int colorPickerProgram;
    private int colorGradientProgram;
    private int connectionProgram;
    
    private Matrix4f projectionMatrix;
    private int quadVAO;
    private int textVAO;
    private int textVBO;
    
    private List<UIElement> visibleUIElements;
    private UIElement rootElement;
    private UIElement focussedElement;
    private UIElement pointerElement;
    private UIElement hoveredElement;
    
    protected Set<Animation> animations;
    protected Map<UIElement, Set<Transition>> transitions;
    
    private UIPopup fullScreenPopup;
    private UIRectangle fullScreenPopupBG;

    private UIBanner banner;
    
    private UIAArea uiaArea;
    private UIInspectorPane inspectorPane;
    
    public static UIRenderer getInstance() {
        if (instance==null) instance = new UIRenderer();
        return instance;
    }
    
    private UIRenderer() {
        visibleUIElements = new ArrayList<>();
        animations = new HashSet<>();
        transitions = new HashMap<>();
    }
    
    private void loadUIResources() {
        UITextureManager.loadTexture("up_arrow.png");
        UITextureManager.loadTexture("down_arrow.png");
        UITextureManager.loadTexture("cube.png");
        UITextureManager.loadTexture("light.png");
        UITextureManager.loadTexture("data.png");
        UITextureManager.loadTexture("close.png");

        UITextureManager.loadTexture("tick_circle.png");
    }
    
    private void buildBase() {
        UIRectangle base = UIRectFactory.sized(sw(1f), sh(1f));
        base.style("color", UIColor.black());
        
        uiaArea = new UIAArea();
        uiaArea.style("width", pcw(0.6f))
        .style("height", pch(1f))
        .style("position", UIPositionMode.ABSOLUTE);
        
        inspectorPane = new UIInspectorPane();
        inspectorPane.style("width", pcw(0.4f))
        .style("height", pch(1f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-left", pcw(0.6f));
        
        base.addChild(uiaArea);
        base.addChild(inspectorPane);
        addToRoot(base);
        
        uiaArea.rebuildFromPipeline();
        inspectorPane.loadRenderNode(null);
    }
    
    public UIAArea getUIAArea() {
        return uiaArea;
    }
    
    public UIInspectorPane getInspectorPane() {
        return inspectorPane;
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
        if (transitions.containsKey(e)) {
            for (Transition eT : transitions.get(e)) {
                if (eT.hasTag()&&t.hasTag()&&eT.getTag().equals(t.getTag())) eT.interrupt();
            }
        }
        else transitions.put(e, new HashSet<>());
        transitions.get(e).add(t);
    }
    
    public void createFullScreenPopup(UIPopup popup) {
        if (fullScreenPopup!=null) return;
        
        fullScreenPopupBG = new UIRectangle();
        fullScreenPopupBG.style("width", sw(1f))
        .style("height", sh(1f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("color", UIColor.black())
        .style("opacity", 0.5f)
        .style("z-index", 999)
        .clickable(true)
        .onClicked(e -> popup.close(false));
        
        popup.style("z-index", 1000);
        popup.addClosedAction(s -> {
            rootElement.removeChild(fullScreenPopupBG);
            fullScreenPopup = null;
        });
        
        fullScreenPopup = popup;
        rootElement.addChild(fullScreenPopupBG);
        rootElement.addChild(fullScreenPopup);
    }

    public void createBanner(UIBannerMode mode, String title, String message) {
        if (banner!=null) rootElement.removeChild(banner);

        banner = new UIBanner(mode, title, message);
        banner.style("z-index", 999)
        .style("offset-left", px(20));
        addToRoot(banner);

        banner.fadeOut(2);
    }

    public void removeBanner(UIBanner banner) {
        if (banner==null) return;
        rootElement.removeChild(banner);
        this.banner = null;
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
        .style("color", UIColor.black())
        .style("position", UIPositionMode.SCREEN)
        .style("box", UIBoxMode.FIXED)
        .style("flow-direction", UIFlowDirection.COLUMN);
        
        rootElement.markLayoutDirty();
        rootElement.markSubtreeDirty();
        
        ShaderManager sM = ShaderManager.getInstance();
        
        shapeProgram = sM.createProgram("ui/ui_shapes.vert", "ui/ui_shapes.frag");
        textProgram = sM.createProgram("ui/ui_text.vert", "ui/ui_text.frag");
        iconProgram = sM.createProgram("ui/ui_icon.vert", "ui/ui_icon.frag");
        colorPickerProgram = sM.createProgram("ui/ui_shapes.vert", "ui/ui_colorpicker.frag");
        colorGradientProgram = sM.createProgram("ui/ui_shapes.vert", "ui/ui_colorgradient.frag");
        connectionProgram = sM.createProgram("ui/ui_shapes.vert", "ui/ui_connection.frag");
        
        // Shape setup
        quadVAO = glGenVertexArrays();
        int quadVBO = glGenBuffers();
        sM.bindVAO(quadVAO);
        sM.bindVBO(quadVBO);
        glBufferData(GL_ARRAY_BUFFER, ShaderManager.QUAD_VERTICES, GL_STATIC_DRAW);
        
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
        
        loadUIResources();
        buildBase();
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
            
            UIElement hit = rootElement.getDeepestVisibleElementAt(event.getMouseX(), event.getMouseY());
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
            
            case SCROLL:
            
            hit = rootElement.getDeepestVisibleElementAt(event.getMouseX(), event.getMouseY());
            if (hit == null) return;
            
            UIElement scrollReceiver = hit.findAncestorMatching(UIElement::isScrollable);
            if (scrollReceiver != null) {
                scrollReceiver.handleIOEvent(event);
                return;
            }
            
            UIElement overflowScrollTarget = hit.findAncestorMatching(e ->
                e.getOverflowY() == UIOverflowMode.SCROLL ||
                e.getOverflowX() == UIOverflowMode.SCROLL
            );
            
            if (overflowScrollTarget!=null) {
                overflowScrollTarget.scrollY(overflowScrollTarget.getScrollY() - event.getScrollY() * 0.05f);
            }
            
            return;
            
            case LEFT_PRESS:
            hit = rootElement.getDeepestVisibleElementAt(event.getMouseX(), event.getMouseY());
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
    
    public void setFocussedElement(UIElement e) {
        if (focussedElement!=null) focussedElement.lostFocus(IOEvent.dummyEvent());
        focussedElement = e;
        if (focussedElement!=null) focussedElement.gotFocus(IOEvent.dummyEvent());
    }
    
    public void buildVisibleElementList() {
        visibleUIElements = rootElement.getVisibleChildren();
        rootElement.clearSubtreeDirtyMark();
    }
    
    private void layout() {
        rootElement.layoutCalculate();
        rootElement.layoutPlace(
            0f,
            0f, 
            new UIBounds(
                0f,
                0f,
                getParentWindow().getWidth(),
                getParentWindow().getHeight()
            )
        );
        
        rootElement.layoutUpdatedSubtree();
    }
    
    @Override
    public void update() {
        // Animations
        for (Animation a : animations) a.trigger();
        
        // Transitions
        for (Map.Entry<UIElement, Set<Transition>> entry : transitions.entrySet()) {
            for (Transition t : entry.getValue()) t.update();
            entry.getValue().removeIf(e -> e.isComplete()||e.isInterrupted());
        }
        transitions.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        // Opacity
        if (rootElement.isOpacityDirty()) rootElement.updateEffectiveOpacity(1f);
        
        // Layout
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
        
        ShaderManager sM = ShaderManager.getInstance();
        
        // Draw objects
        for (UIElement e : visibleUIElements) e.render(this, sM);
        
        // Reset
        glActiveTexture(GL_TEXTURE0);
        sM.bindVAO(0);
        sM.useProgram(0);
    }
    
    public void renderShape(ShaderManager sM, UIElement e) {
        sM.useProgram(shapeProgram);
        sM.setCurrentProgram(shapeProgram);
        
        sM.setUniformMat4("uProjection", projectionMatrix);
        sM.setUniformMat4("uModel", e.getModelMatrix());
        e.setRenderUniforms(sM);
        
        sM.bindVAO(quadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }
    
    public void renderText(ShaderManager sM, UIText tE) {
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
    
    public void renderIcon(ShaderManager sM, UIIcon icon) {
        sM.useProgram(iconProgram);
        sM.setCurrentProgram(iconProgram);
        
        sM.setUniformMat4("uProjection", projectionMatrix);
        
        // Fit mode
        Matrix4f model = icon.getModelMatrix();
        
        if (icon.getFitMode() == UIIcon.UIIconFitMode.FIT) {
            float boxW = icon.getScreenWidth();
            float boxH = icon.getScreenHeight();
            
            float texW = icon.getTexture().getWidth();
            float texH = icon.getTexture().getHeight();
            
            float scale = Math.min(boxW / texW, boxH / texH);
            
            float fittedW = texW * scale;
            float fittedH = texH * scale;
            
            float x = icon.getScreenX() + (boxW - fittedW) * 0.5f;
            float y = icon.getScreenY() + (boxH - fittedH) * 0.5f;
            
            model = new Matrix4f()
            .translation(x + fittedW * 0.5f, y + fittedH * 0.5f, 0f)
            .scale(fittedW, fittedH, 1f);
        }
        
        sM.setUniformMat4("uModel", model);       
        
        // Other uniforms
        UIColor tint = icon.getTint();
        sM.setUniformVec4("uTint", new Vec4(tint.getRed(), tint.getGreen(), tint.getBlue(), tint.getAlpha()));
        sM.setUniformVec4("uUVRect", icon.getUVRect());
        
        icon.setRenderUniforms(sM);
        
        glActiveTexture(GL_TEXTURE0);
        sM.bindTexture(icon.getTextureId());
        sM.setUniformInt("uTexture", 0);
        
        sM.bindVAO(quadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }
    
    public void renderTextureView(ShaderManager sM, UITextureView view) {
        sM.useProgram(iconProgram);
        sM.setCurrentProgram(iconProgram);
        
        sM.setUniformMat4("uProjection", projectionMatrix);
        
        Matrix4f model = view.getModelMatrix();
        sM.setUniformMat4("uModel", model);  
        sM.setUniformVec4("uTint", new Vec4(1f, 1f, 1f, 1f));
        sM.setUniformVec4("uUVRect", new Vec4(0f, 0f, 1f, 1f));     
        
        view.setRenderUniforms(sM);
        
        glActiveTexture(GL_TEXTURE0);
        sM.bindTexture(view.getTextureId());
        sM.setUniformInt("uTexture", 0);
        
        sM.bindVAO(quadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }
    
    public void renderColorPicker(ShaderManager sM, UIColorPicker cP) {
        sM.useProgram(colorPickerProgram);
        sM.setCurrentProgram(colorPickerProgram);
        
        sM.setUniformMat4("uProjection", projectionMatrix);
        sM.setUniformMat4("uModel", cP.getModelMatrix());
        cP.setRenderUniforms(sM);
        
        sM.bindVAO(quadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }
    
    public void renderColorGradient(ShaderManager sM, UIElement e) {
        sM.useProgram(colorGradientProgram);
        sM.setCurrentProgram(colorGradientProgram);
        
        sM.setUniformMat4("uProjection", projectionMatrix);
        sM.setUniformMat4("uModel", e.getModelMatrix());
        e.setRenderUniforms(sM);
        
        sM.bindVAO(quadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }
    
    public void renderConnection(ShaderManager sM, UIConnection e) {
        sM.useProgram(connectionProgram);
        sM.setCurrentProgram(connectionProgram);
        
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
