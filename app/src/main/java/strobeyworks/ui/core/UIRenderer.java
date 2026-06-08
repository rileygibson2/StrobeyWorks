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
import static strobeyworks.ui.core.UILength.pbh;
import static strobeyworks.ui.core.UILength.pbw;
import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.pph;
import static strobeyworks.ui.core.UILength.ppw;
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
import strobeyworks.noiserender.AgentRenderer;
import strobeyworks.noiserender.NoiseRenderer;
import strobeyworks.platform.Animation;
import strobeyworks.platform.IOEvent;
import strobeyworks.platform.Renderer;
import strobeyworks.platform.ShaderManager;
import strobeyworks.platform.Transition;
import strobeyworks.ui.components.UIButton;
import strobeyworks.ui.components.UIColorPicker;
import strobeyworks.ui.components.UITab;
import strobeyworks.ui.components.input.UICheckBox;
import strobeyworks.ui.components.input.UISlider;
import strobeyworks.ui.components.input.UIValueAdaptor;
import strobeyworks.ui.components.input.field.UIFieldRule;
import strobeyworks.ui.components.input.field.UIFloatField;
import strobeyworks.ui.components.input.field.UIFloatFieldRule;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIElement.UIAlignContent;
import strobeyworks.ui.primitives.UIElement.UIAlignItems;
import strobeyworks.ui.primitives.UIElement.UIBoxMode;
import strobeyworks.ui.primitives.UIElement.UIFlowDirection;
import strobeyworks.ui.primitives.UIElement.UIOverflowMode;
import strobeyworks.ui.primitives.UIElement.UIPositionMode;
import strobeyworks.ui.primitives.UIIcon;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.utils.Bindable;
import strobeyworks.utils.Vec4;

public class UIRenderer extends Renderer {
    
    private static UIRenderer instance;
    
    private int shapeProgram;
    private int textProgram;
    private int iconProgram;
    private int colorPickerProgram;
    
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
    
    private UITab mainTab;
    
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
        UIFontManager.loadFont("RobotoMono-Medium.ttf", 30f);
        UIFontManager.loadFont("RobotoMono-Medium.ttf", 25f);
        UIFontManager.loadFont("RobotoMono-Medium.ttf", 20f);
        UIFontManager.loadFont("RobotoMono-Medium.ttf", 15f);
        UIFontManager.loadFont("RobotoMono-Medium.ttf", 10f);
        
        UITextureManager.loadTexture("up_arrow.png");
        UITextureManager.loadTexture("down_arrow.png");
        UITextureManager.loadTexture("cube.png");
        UITextureManager.loadTexture("light.png");
        UITextureManager.loadTexture("data.png");
    }
    
    private void buildBase() {
        mainTab = new UITab(pbw(1f), pbh(1f), UIFontManager.getUIFont("RobotoMono-Medium.ttf", 20f));
        addToRoot(mainTab);
    }
    
    public void buildNoiseTab() {
        UIRectangle pane = new UIRectangle();
        pane.style("width", ppw(1f))
        .style("height", pph(1f))
        .style("padding-left", px(5))
        .style("padding-right", px(5))
        .style("padding-top", px(5))
        .style("padding-bottom", px(5))
        .style("align-content", UIAlignContent.CENTER)
        .style("flow-direction", UIFlowDirection.COLUMN)
        .style("color", UIColor.TRANSPARENT)
        .style("overflow-y", UIOverflowMode.SCROLL);
        
        mainTab.addTab("OBJECTS", "cube", pane);
        
        NoiseRenderer r = NoiseRenderer.getInstance();
        
        record FloatControlConfig(
            String name,
            Bindable<Float> binding,
            float min,
            float max,
            int precision,
            float butIncrement
        ) {}
        
        record BooleanControlConfig(
            String name,
            Bindable<Boolean> binding
        ) {}
        
        FloatControlConfig[] sliderControls = {
            new FloatControlConfig("Speed", r.getSpeed(), 0f, 5f, 1, 0.1f),
            new FloatControlConfig("Scale", r.getGridSize(), 2f, 50f, 0, 1f),
            new FloatControlConfig("Octaves", r.getOctaves(), 1f, 10f, 0, 1f),
            new FloatControlConfig("Gamma", r.getGamma(), 0.1f, 20f, 1, 0.1f),
            new FloatControlConfig("Gain", r.getGain(), 1f, 200f, 1, 1f),
            new FloatControlConfig("Warp Strength", r.getWarpStrength(), 0f, 10f, 1, 0.1f),
            new FloatControlConfig("Warp Scale", r.getWarpScale(), 0.1f, 5f, 2, 0.1f),
            new FloatControlConfig("Ridge Scale", r.getRidgePow(), 1f, 10f, 0, 1f),
            new FloatControlConfig("Turbulence Scale", r.getTurbulencePow(), 0.25f, 8f, 1, 0.1f)
        };
        
        BooleanControlConfig[] cbControls = {
            new BooleanControlConfig("Warp", r.getWarp()),
            new BooleanControlConfig("Octave Ridge", r.getOctaveRidge()),
            new BooleanControlConfig("Post Ridge", r.getPostRidge()),
            new BooleanControlConfig("Octave Turbulence", r.getOctaveTurbulence())
        };
        
        UIFont titleFont = UIFontManager.getUIFont("RobotoMono-Medium.ttf", 20f);
        UIFont fieldFont = UIFontManager.getUIFont("RobotoMono-Medium.ttf", 20f);
        
        UIRectangle line = new UIRectangle();
        line.style("width", pcw(1f))
        .style("box", UIBoxMode.FLEX)
        //.style("height", pch(0.08f))
        .style("margin-top", px(10))
        .style("max-width", pcw(1f))
        .style("align-items", UIAlignItems.CENTER)
        //.style("color", col(UIColors.RED))
        .style("flow-wrap", true);
        pane.addChild(line);
        
        for (BooleanControlConfig config : cbControls) {
            UIText title = new UIText(titleFont, config.name());
            title.style("margin-left", px(10))
            .style("color", UIColor.GREEN);
            
            UICheckBox cB = new UICheckBox(px(40), px(40), true);
            cB.style("margin-left", px(10));
            cB.bindTo(config.binding());
            
            line.addChild(title);
            line.addChild(cB);
            
        }
        
        for (FloatControlConfig config : sliderControls) {
            line = new UIRectangle();
            line.style("width", pcw(1f))
            .style("height", pch(0.08f))
            //.style("color", col(UIColors.RED))
            .style("margin-top", px(10))
            .style("align-items", UIAlignItems.CENTER);
            
            UIRectangle right = new UIRectangle();
            right.style("width", ppw(0.8f))
            .style("height", pph(1f))
            .style("position", UIPositionMode.ABSOLUTE)
            .style("offset-left", ppw(0.2f))
            .style("color", UIColor.TRANSPARENT)
            .style("margin-top", px(10))
            .style("align-items", UIAlignItems.CENTER);
            
            UIText title = new UIText(titleFont, config.name());
            title.style("margin-left", px(10))
            .style("color", UIColor.GREEN);
            
            UIFloatFieldRule inputRule = UIFieldRule.defaultFloat();
            inputRule.maxCharacters(3)
            .maxPrecision(config.precision())
            .inputMinMax(config.min(), config.max());
            
            UIFloatField field = new UIFloatField(fieldFont, inputRule);
            field.useButtons(config.butIncrement());
            field.style("width", ppw(0.2f))
            .style("height", pph(1f))
            .style("margin-left", ppw(0.05f));
            
            UISlider slider = new UISlider(
                ppw(0.65f),
                pph(1f),
                UIValueAdaptor.floatRange(config.min(), config.max())
            );
            slider.style("margin-left", ppw(0.1f));
            
            field.bindTo(config.binding());
            slider.bindTo(config.binding);
            
            line.addChild(title);
            line.addChild(right);
            right.addChild(slider);
            right.addChild(field);
            pane.addChild(line);
        }
    }
    
    public void buildAgentTab() {
        UIRectangle pane = new UIRectangle();
        pane.style("width", ppw(1f))
        .style("height", pph(1f))
        .style("padding-left", px(5))
        .style("padding-right", px(5))
        .style("padding-top", px(5))
        .style("padding-bottom", px(5))
        .style("align-content", UIAlignContent.CENTER)
        .style("flow-direction", UIFlowDirection.COLUMN)
        .style("color", UIColor.TRANSPARENT)
        .style("overflow-y", UIOverflowMode.SCROLL);
        
        mainTab.addTab("AGENTS", "cube", pane);
        
        AgentRenderer r = AgentRenderer.getInstance();
        UIFont titleFont = UIFontManager.getUIFont("RobotoMono-Medium.ttf", 20f);
        UIFont fieldFont = UIFontManager.getUIFont("RobotoMono-Medium.ttf", 20f);
        
        Bindable<UIColor> pickerColor = Bindable.of(UIColor.rgb(1f, 1f, 1f));


        UIColorPicker colPick = new UIColorPicker();
        colPick.style("width", pcw(0.2f))
        .style("height", pch(0.2f));
        colPick.bindColor(pickerColor);

        UIRectangle ind = new UIRectangle();
        ind.style("width", pcw(0.2f))
        .style("height", pch(0.2f))
        .style("margin-left", px(20))
        .style("color", pickerColor.getValue());

        //pane.addChild(colPick);
        //pane.addChild(ind);



        UIButton but = new UIButton(fieldFont, "Restore");
        but.style("width", pcw(0.2f))
        .style("height", px(30))
        .onClicked(e -> {
            r.loadDefaults();
        });
        pane.addChild(but);
        
        but = new UIButton(fieldFont, "Randomize");
        but.style("width", pcw(0.2f))
        .style("height", px(30))
        .onClicked(e -> {
            r.randomize();
        });
        pane.addChild(but);
        
        record FloatControlConfig(
            String name,
            Bindable<Float> binding,
            float min,
            float max,
            int precision,
            float butIncrement
        ) {}
        
        record BooleanControlConfig(
            String name,
            Bindable<Boolean> binding
        ) {}
        
        FloatControlConfig[] sliderControls = {
            new FloatControlConfig("Diffusion", r.getDiffusion(), 0f, 1.5f, 2, 0.01f),
            new FloatControlConfig("Decay", r.getDecay(), 0f, 10f, 2, 1f),
            new FloatControlConfig("Speed", r.getSpeed(), 0f, 1f, 2, 0.01f),
            new FloatControlConfig("Sensor Angle", r.getSensorAngle(), 0f, 3f, 2, 0.1f),
            new FloatControlConfig("Sensor Distance", r.getSensorDistance(), 0f, 0.5f, 2, 0.01f),
            new FloatControlConfig("Random", r.getRandomTurnStrength(), 0f, 20f, 2, 0.01f),
            new FloatControlConfig("Turn Speed", r.getTurnSpeed(), 0.2f, 20f, 2, 1f),
            new FloatControlConfig("Opacity Cuttoff", r.getOpacityCuttoff(), 0f, 1f, 2, 0.01f),
            new FloatControlConfig("Contribution", r.getPheramoneContribution(), 0f, 1f, 2, 0.01f)
        };
        
        BooleanControlConfig[] cbControls = {
        };
        
        UIRectangle line = new UIRectangle();
        line.style("width", pcw(1f))
        .style("box", UIBoxMode.FLEX)
        //.style("height", pch(0.08f))
        .style("margin-top", px(10))
        .style("max-width", pcw(1f))
        .style("align-items", UIAlignItems.CENTER)
        //.style("color", col(UIColors.RED))
        .style("flow-wrap", true);
        pane.addChild(line);
        
        for (BooleanControlConfig config : cbControls) {
            UIText title = new UIText(titleFont, config.name());
            title.style("margin-left", px(10))
            .style("color", UIColor.GREEN);
            
            UICheckBox cB = new UICheckBox(px(40), px(40), true);
            cB.style("margin-left", px(10));
            cB.bindTo(config.binding());
            
            line.addChild(title);
            line.addChild(cB);
            
        }
        
        for (FloatControlConfig config : sliderControls) {
            line = new UIRectangle();
            line.style("width", pcw(1f))
            .style("height", pch(0.08f))
            //.style("color", col(UIColors.RED))
            .style("margin-top", px(10))
            .style("align-items", UIAlignItems.CENTER);
            
            UIRectangle right = new UIRectangle();
            right.style("width", ppw(0.8f))
            .style("height", pph(1f))
            .style("position", UIPositionMode.ABSOLUTE)
            .style("offset-left", ppw(0.2f))
            .style("color", UIColor.TRANSPARENT)
            .style("margin-top", px(10))
            .style("align-items", UIAlignItems.CENTER);
            
            UIText title = new UIText(titleFont, config.name());
            title.style("margin-left", px(10))
            .style("color", UIColor.GREEN);
            
            UIFloatFieldRule inputRule = UIFieldRule.defaultFloat();
            inputRule.maxCharacters(3)
            .maxPrecision(config.precision())
            .inputMinMax(config.min(), config.max());
            
            UIFloatField field = new UIFloatField(fieldFont, inputRule);
            field.useButtons(config.butIncrement());
            field.style("width", ppw(0.2f))
            .style("height", pph(1f))
            .style("margin-left", ppw(0.05f));
            
            UISlider slider = new UISlider(
                ppw(0.65f),
                pph(1f),
                UIValueAdaptor.floatRange(config.min(), config.max())
            );
            slider.style("margin-left", ppw(0.1f));
            
            field.bindTo(config.binding());
            slider.bindTo(config.binding);
            
            line.addChild(title);
            line.addChild(right);
            right.addChild(slider);
            right.addChild(field);
            pane.addChild(line);
        }
    }
    
    public void buildLightsTab() {
        UIRectangle pane = new UIRectangle();
        pane.style("width", ppw(1f))
        .style("height", pph(1f))
        .style("padding-left", px(5))
        .style("padding-right", px(5))
        .style("padding-top", px(5))
        .style("padding-bottom", px(5))
        .style("align-items", UIAlignItems.CENTER)
        .style("align-content", UIAlignContent.CENTER)
        .style("flow-direction", UIFlowDirection.COLUMN)
        .style("color", UIColor.BLUE);
        
        mainTab.addTab("LIGHTS", "light", pane);
    }
    
    public void buildDataTab() {
        mainTab.addTab("DATA", "data", null);
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
        .style("color", UIColor.BLACK)
        .style("position", UIPositionMode.SCREEN)
        .style("box", UIBoxMode.FIXED)
        .style("flow-direction", UIFlowDirection.COLUMN);
        
        rootElement.markLayoutDirty();
        rootElement.markSubtreeDirty();
        
        ShaderManager sM = SWMain.getShaderManager();
        
        shapeProgram = sM.createProgram("ui/ui_shapes.vert", "ui/ui_shapes.frag");
        textProgram = sM.createProgram("ui/ui_text.vert", "ui/ui_text.frag");
        iconProgram = sM.createProgram("ui/ui_icon.vert", "ui/ui_icon.frag");
        colorPickerProgram = sM.createProgram("ui/ui_shapes.vert", "ui/ui_colorpicker.frag");
        
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
        buildAgentTab();
        
        mainTab.setTab(0);
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
            
            case SCROLL:
            
            hit = rootElement.getDeepestElementAt(event.getMouseX(), event.getMouseY());
            if (hit == null) return;
            
            UIElement scrollTarget = hit.findAncestorMatching(e ->
                e.getOverflowY() == UIOverflowMode.SCROLL ||
                e.getOverflowX() == UIOverflowMode.SCROLL
            );
            
            if (scrollTarget!=null) {
                scrollTarget.scrollY(scrollTarget.getScrollY() - event.getScrollY() * 0.05f);
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
        // Animations and transitions
        for (Animation a : animations) a.trigger();
        
        for (Map.Entry<UIElement, Set<Transition>> entry : transitions.entrySet()) {
            for (Transition t : entry.getValue()) t.update();
            entry.getValue().removeIf(e -> e.isComplete()||e.isInterrupted());
        }
        transitions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        
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
        
        ShaderManager sM = SWMain.getShaderManager();
        
        // Draw objects
        for (UIElement e : visibleUIElements) {
            if (e instanceof UIText) renderText(sM, (UIText) e);
            else if (e instanceof UIIcon) renderIcon(sM, (UIIcon) e);
            else if (e instanceof UIColorPicker) renderColorPicker(sM, (UIColorPicker) e);
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

    private void renderColorPicker(ShaderManager sM, UIColorPicker cP) {
        sM.useProgram(colorPickerProgram);
        sM.setCurrentProgram(colorPickerProgram);

        sM.setUniformMat4("uProjection", projectionMatrix);
        sM.setUniformMat4("uModel", cP.getModelMatrix());
        cP.setRenderUniforms(sM);

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
