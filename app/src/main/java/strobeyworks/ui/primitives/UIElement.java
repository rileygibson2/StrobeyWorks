package strobeyworks.ui.primitives;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.px;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.joml.Matrix4f;

import strobeyworks.SWMain;
import strobeyworks.logger.Logger;
import strobeyworks.platform.IOEvent;
import strobeyworks.platform.ShaderManager;
import strobeyworks.platform.Transition;
import strobeyworks.ui.components.UIScrollBar;
import strobeyworks.ui.components.UIScrollBar.ScrollAxis;
import strobeyworks.ui.core.UIBounds;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.core.UIRenderer;
import strobeyworks.ui.style.StyleProps;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.ui.style.UIStyleProperty;
import strobeyworks.utils.Utils;
import strobeyworks.utils.Vec4;

/**
* 
* BOX MODEL:
* 
* An element has three nested boxes;
* Border box is the total element width and height
* Padding box is the border box minus border thickness (if present)
* Content box is the padding box minus the padding on all sides (if present)
* 
* Authored width and height values for an element affect the size of the border box.
* That is to say the total size of the element. If a border or padding is specified it decreases the size
* of the content box, it will not increase the total size of the element.
* Max and min width and height values also specify the size of the border box.
* 
* POSITIONING:
* 
* SCREEN elements will use the top left of the screen as an origin and consider authored offset values.
* These elements will not participate in the layout flow.
* 
* ABSOLUTE elements will use the padding box top left as an origin and consider authored offset values.
* These elements will not participate in the layout flow.
* 
* FLOW elements will use the content box top left as an origin and consider authored margin values.
* These elements will be positioned in the normal layout flow, relative to their flow siblings.
* 
* FLOW_RELATIVE elements will behave the same as FLOW elements, but will also consider authored offset values.
* These offset values will be applied from the flow layout position and will not affect the flow of their
* siblings.
* 
* CENTERING:
*
* JUSTIFY_CONTENT CENTER centers flow children along the main axis.
* For ROW flow, children are centered horizontally.
* For COLUMN flow, children are centered vertically.
*
* ALIGN_ITEMS CENTER centers children along the cross axis within their line.
* Child margins are included.
*
* ALIGN_CONTENT CENTER centers wrapped flow lines along the cross axis.
*
* UNITS:
*
* PIXELS: raw pixels.
* SCREEN_WIDTH / SCREEN_HEIGHT: percentage of screen size.
* PARENT_BORDER_BOX_WIDTH / HEIGHT: percentage of parent border box.
* PARENT_PADDING_BOX_WIDTH / HEIGHT: percentage of parent padding box.
* PARENT_CONTENT_BOX_WIDTH / HEIGHT: percentage of parent content box.
*
* Parent-relative units require a non-FLEX parent, because FLEX parents may not
* know their final size when children are being measured.
* 
*/
public abstract class UIElement {
    
    /**
    * Sets the behaviour of the element size with respect to it's children
    */
    public enum UIBoxMode {
        /**
        * Element size stays constant.
        */
        FIXED,
        /**
        * Element will grow and shrink to fit content and respect max and min width and height
        */
        FLEX
    }
    
    /**
    * Sets the main axis direction of children element positioning flow
    */
    public enum UIFlowDirection {
        /**
        * Children elements will be flowed from the left of this box to the right.
        */
        ROW,
        /**
        * Children elements will be flowed from the top of this box to the bottom.
        */
        COLUMN
    }
    
    /**
    * Controls how this element is positioned relative to it's parent or the screen.
    */
    public enum UIPositionMode {
        /**
        * Participates in flow within parent's content box.
        * Margin affects flow layout.
        * Offset is ignored.
        * Can be clipped by parent and offset by scroll.
        */
        FLOW,
        /**
        * Participates in flow within parent's content box.
        * Margin affects flow layout.
        * Offset affects final position but does not affect sibling flow layout.
        * Can be clipped by parent and can be offset by scroll.
        */
        FLOW_RELATIVE,
        /**
        * Removed from parent flow and positioned relative to parent content box.
        * Margin is ignored.
        * Offset is respected.
        * Can be clipped by parent and can be offset by scroll.
        */
        ABSOLUTE,
        /**
        * Removed from parent flow and positioned relative to parent content box.
        * Margin is ignored.
        * Offset is respected.
        * Can be clipped by parent but cannot be offset by scroll.
        */
        ABSOLUTE_FIXED,
        /**
        * Removed from parent flow and positioned relative to screen bounds.
        * Margin is ignored.
        * Offset is respected.
        * Cannot be clipped by parent or offset by scroll.
        */
        SCREEN
    }
    
    /**
    * Sets how flowed elements will be justified along the flow direction main axis
    */
    public enum UIJustifyContent {
        /**
        * Content will be justified to the start of the main axis.
        * If wrapped then items will be justified to the start of the main axis in each logical line.
        */
        START,
        /**
        * Content will be justified to the center of the main axis.
        * If wrapped then items will be justified to the center of the main axis in each logical line.
        */
        CENTER
    }
    
    /**
    * Sets how flowed elements will be aligned along the flow direction cross axis
    */
    public enum UIAlignItems {
        /**
        * Items will be aligned to the start of the cross axis.
        * If wrapped then items will be aligned to the start of the cross axis in each logical line.
        */
        START,
        /**
        * Items will be aligned to the center of the cross axis.
        * If wrapped then items will be aligned to the center of the cross axis in each logical line.
        */
        CENTER
    }
    
    /**
    * Sets how wrapped line as a unit is aligned along the cross axis within the parent content box.
    * Only has effect when flow wrapping creates multiple lines.
    */
    public enum UIAlignContent {
        /**
        * Wrapped lines as a unit will be aligned from the start of the cross axis in the parent content box.
        */
        START,
        /**
        * Wrapped lines as a unit will be aligned with the center of the cross axis in the parent content box.
        */
        CENTER
    }
    
    /**
    * 
    */
    public enum UIOverflowMode {
        /**
        * 
        */
        VISIBLE,
        /**
        * 
        */
        HIDDEN,
        /**
        * 
        */
        SCROLL
    }
    
    @FunctionalInterface
    public interface UIClickCallback {
        void implement(float x, float y, boolean leftButton, boolean rightButton);
    }
    
    // Tree
    private UIElement parent;
    private List<UIElement> children;
    private boolean layoutDirty; // Object layout has changed
    private boolean subtreeDirty; // Composition of subtree has changed
    private boolean initialised; // Element has been initialised (also indicator of computed layout values existing)
    
    // Authored style properties
    private UIBoxMode boxMode;
    private UIFlowDirection flowDirection;
    private boolean flowWrap;
    private UIPositionMode positionMode;
    private UIJustifyContent justifyContent;
    private UIAlignItems alignItems;
    private UIAlignContent alignContent;
    private UIOverflowMode overflowX;
    private UIOverflowMode overflowY;
    
    private UILength width;
    private UILength height;
    
    private UILength paddingLeft;
    private UILength paddingRight;
    private UILength paddingTop;
    private UILength paddingBottom;
    
    private UILength marginLeft;
    private UILength marginRight;
    private UILength marginTop;
    private UILength marginBottom;
    
    private UILength offsetLeft;
    private UILength offsetRight;
    private UILength offsetTop;
    private UILength offsetBottom;
    
    private Boolean borderEnabled;
    private UILength borderThickness;
    private boolean borderLeft;
    private boolean borderRight;
    private boolean borderTop;
    private boolean borderBottom;
    
    private UILength minWidth;
    private UILength minHeight;
    
    private UILength maxWidth;
    private UILength maxHeight;
    
    private float transformScaleX;
    private float transformScaleY;
    
    private float transitionDuration;
    
    private float opacity;
    
    private boolean visible;
    
    private float scrollX;
    private float scrollY;
    
    // Style appliers
    private static final Map<UIStyleProperty<?>, BiConsumer<UIElement, Object>> APPLIERS = new HashMap<>();
    
    static {
        register(APPLIERS, StyleProps.BOX, UIElement::box);
        register(APPLIERS, StyleProps.FLOW_DIRECTION, UIElement::flowDirection);
        register(APPLIERS, StyleProps.FLOW_WRAP, UIElement::flowWrap);
        register(APPLIERS, StyleProps.POSITION, UIElement::position);
        register(APPLIERS, StyleProps.JUSTIFY_CONTENT, UIElement::justifyContent);
        register(APPLIERS, StyleProps.ALIGN_ITEMS, UIElement::alignItems);
        register(APPLIERS, StyleProps.ALIGN_CONTENT, UIElement::alignContent);
        register(APPLIERS, StyleProps.OVERFLOW_X, UIElement::overflowX);
        register(APPLIERS, StyleProps.OVERFLOW_Y, UIElement::overflowY);
        
        register(APPLIERS, StyleProps.WIDTH, UIElement::width);
        register(APPLIERS, StyleProps.HEIGHT, UIElement::height);
        register(APPLIERS, StyleProps.MIN_WIDTH, UIElement::minWidth);
        register(APPLIERS, StyleProps.MIN_HEIGHT, UIElement::minHeight);
        register(APPLIERS, StyleProps.MAX_WIDTH, UIElement::maxWidth);
        register(APPLIERS, StyleProps.MAX_HEIGHT, UIElement::maxHeight);
        
        register(APPLIERS, StyleProps.MARGIN_LEFT, UIElement::marginLeft);
        register(APPLIERS, StyleProps.MARGIN_RIGHT, UIElement::marginRight);
        register(APPLIERS, StyleProps.MARGIN_TOP, UIElement::marginTop);
        register(APPLIERS, StyleProps.MARGIN_BOTTOM, UIElement::marginBottom);
        
        register(APPLIERS, StyleProps.PADDING_LEFT, UIElement::paddingLeft);
        register(APPLIERS, StyleProps.PADDING_RIGHT, UIElement::paddingRight);
        register(APPLIERS, StyleProps.PADDING_TOP, UIElement::paddingTop);
        register(APPLIERS, StyleProps.PADDING_BOTTOM, UIElement::paddingBottom);
        
        register(APPLIERS, StyleProps.OFFSET_LEFT, UIElement::offsetLeft);
        register(APPLIERS, StyleProps.OFFSET_RIGHT, UIElement::offsetRight);
        register(APPLIERS, StyleProps.OFFSET_TOP, UIElement::offsetTop);
        register(APPLIERS, StyleProps.OFFSET_BOTTOM, UIElement::offsetBottom);
        
        register(APPLIERS, StyleProps.OPACITY, UIElement::opacity);
        register(APPLIERS, StyleProps.VISIBLE, UIElement::visible);
        
        register(APPLIERS, StyleProps.BORDER_ENABLED, UIElement::borderEnabled);
        register(APPLIERS, StyleProps.BORDER_THICKNESS, UIElement::borderThickness);
        register(APPLIERS, StyleProps.BORDER_LEFT, UIElement::borderLeft);
        register(APPLIERS, StyleProps.BORDER_RIGHT, UIElement::borderRight);
        register(APPLIERS, StyleProps.BORDER_TOP, UIElement::borderTop);
        register(APPLIERS, StyleProps.BORDER_BOTTOM, UIElement::borderBottom);
        
        register(APPLIERS, StyleProps.TRANSITION_DURATION, UIElement::transitionDuration);
        register(APPLIERS, StyleProps.TRANSFORM_SCALEX, UIElement::transformScaleX);
        register(APPLIERS, StyleProps.TRANSFORM_SCALEY, UIElement::transformScaleY);
    }
    
    protected static <E extends UIElement, T> void register(
        Map<UIStyleProperty<?>, BiConsumer<E, Object>> appliers,
        UIStyleProperty<T> property,
        BiConsumer<E, T> applier
    ) {
        appliers.put(property, (e, v) -> applier.accept(e, property.getValueType().cast(v)));
    }
    
    // Computed values
    private float localX;
    private float localY;
    private float localWidth;
    private float localHeight;
    
    private float screenX;
    private float screenY;
    private float screenWidth;
    private float screenHeight;
    
    private UIBounds childContentBounds;
    private UIBounds effectiveClip;
    
    // IO
    private boolean focussable;
    private boolean wantsPointer;
    private boolean hoverable;
    private boolean clickable;
    
    // Functional
    private Matrix4f modelMatrix;
    
    private UIScrollBar horizontalBar;
    private UIScrollBar verticalBar;
    
    private Vec4 debugColor = col(UIColors.RED);
    private boolean debugEnabled;
    
    // Styles
    private UIStyle baseStyle;
    private UIStyle hoverStyle;
    
    public UIElement() {
        this.width = px(0);
        this.height = px(0);
        
        this.boxMode = UIBoxMode.FIXED;
        this.flowDirection = UIFlowDirection.ROW;
        this.flowWrap = false;
        this.positionMode = UIPositionMode.FLOW;
        this.justifyContent = UIJustifyContent.START;
        this.alignItems = UIAlignItems.START;
        this.alignContent = UIAlignContent.START;
        this.overflowX = UIOverflowMode.VISIBLE;
        this.overflowY = UIOverflowMode.VISIBLE;
        
        this.paddingLeft = px(0);
        this.paddingRight = px(0);
        this.paddingTop = px(0);
        this.paddingBottom = px(0);
        
        this.marginLeft = px(0);
        this.marginRight = px(0);
        this.marginTop = px(0);
        this.marginBottom = px(0);
        
        this.offsetLeft = px(0);
        this.offsetRight = px(0);
        this.offsetTop = px(0);
        this.offsetBottom = px(0);
        
        this.borderEnabled = false;
        this.borderThickness = px(0);
        this.borderLeft = true;
        this.borderRight = true;
        this.borderTop = true;
        this.borderBottom = true;
        
        this.transformScaleX = 1f;
        this.transformScaleY = 1f;
        
        this.opacity = 1f;
        
        this.visible = true;
        
        this.layoutDirty = true;
        this.subtreeDirty = false;
        
        this.focussable = false;
        this.wantsPointer = false;
        this.hoverable = false;
        this.clickable = false;
        
        this.debugEnabled = false;
        
        this.baseStyle = new UIStyle();
        
        children = new ArrayList<>();
        updateModelMatrix();
    }
    
    public void setRenderUniforms(ShaderManager sM) {
        sM.setUniformFloat("uOpacity", opacity);
        sM.setUniformInt("uHasBorder", borderEnabled ? 1 : 0);
        sM.setUniformFloat("uBorderThickness", resolve(borderThickness));
        sM.setUniformVec4("uBorderSides", new Vec4(
            borderTop ? 1f : 0f,
            borderRight ? 1f : 0f,
            borderBottom ? 1f : 0f,
            borderLeft ? 1f : 0f
        ));
        
        sM.setUniformVec4("uDebugColor", debugColor);
        sM.setUniformInt("uDebugEnabled", debugEnabled ? 1 : 0);
        
        if (effectiveClip!=null) {
            sM.setUniformInt("uClipEnabled", 1);
            sM.setUniformVec4("uClipBounds", effectiveClip.toVec4());
        }
        else sM.setUniformInt("uClipEnabled", 0);
    }
    
    // -----------------------------------------------------------------------------
    // Styling
    // -----------------------------------------------------------------------------
    
    public UIElement style(String n, Object v) {
        UIStyleProperty<?> property = StyleProps.getProperty(n);
        if (property==null) Logger.throwRuntimeException("Unknown style property: '"+n+"'");
        
        setAuthoredStyleProperty(property, v);
        return this;
    }
    
    public UIElement style(UIStyleProperty<?> p, Object v) {
        setAuthoredStyleProperty(p, v);
        return this;
    }

    private void applyStyle(UIStyle style) {
        for (UIStyleProperty<?> property : style.properties()) {
            setAuthoredStyleProperty(property, style.getRaw(property));
        }
    }
    
    protected void setAuthoredStyleProperty(UIStyleProperty<?> property, Object value) {
        applyStyleProperty(property, value);
        baseStyle.setRaw(property, value);
    }
    
    protected void applyStyleProperty(UIStyleProperty<?> property, Object value) {
        BiConsumer<UIElement, Object> applier = APPLIERS.get(property);
        if (applier!=null) applier.accept(this, value);
    }
    
    protected UIStyle captureStyle() {
        UIStyle style = new UIStyle();
        
        style.set(StyleProps.BORDER_ENABLED, borderEnabled);
        style.set(StyleProps.BORDER_THICKNESS, borderThickness);
        style.set(StyleProps.BORDER_LEFT, borderLeft);
        style.set(StyleProps.BORDER_RIGHT, borderRight);
        style.set(StyleProps.BORDER_TOP, borderTop);
        style.set(StyleProps.BORDER_BOTTOM, borderBottom);
        
        style.set(StyleProps.TRANSITION_DURATION, transitionDuration);
        style.set(StyleProps.TRANSFORM_SCALEX, transformScaleX);
        style.set(StyleProps.TRANSFORM_SCALEY, transformScaleY);
        style.set(StyleProps.OPACITY, opacity);
        return style;
    }
    
    public UIElement hoverStyle(UIStyle hoverStyle) {
        this.hoverStyle = hoverStyle;
        return this;
    }
    
    protected void transitionToStyle(UIStyle target, Transition rawTransition) {
        UIStyle current = captureStyle();
        Set<UIStyleProperty<?>> transitionable = new HashSet<>();
        Set<UIStyleProperty<?>> notTransitionable = new HashSet<>();
        
        // Find all valid properties
        for (UIStyleProperty<?> property : current.properties()) {
            if (target.has(property)) {
                if (property.isTransitionable()) transitionable.add(property);
                else notTransitionable.add(property);
            }
        }
        
        // Apply non transitionable properties
        UIStyle immediate = new UIStyle();
        for (UIStyleProperty<?> property : notTransitionable) {
            immediate.setRaw(property, target.get(property));
        }
        applyStyle(immediate);
        
        // Transition all others
        rawTransition.setUpdatedAction(progress -> {
            UIStyle frame = new UIStyle();
            
            for (UIStyleProperty<?> property : transitionable) {
                Object from = current.get(property);
                Object to = target.get(property);
                
                if (from instanceof Float) {
                    frame.setRaw(property, Utils.lerpFloat((float) from, (float) to, progress));
                }
                else if (from instanceof Vec4) {
                    frame.setRaw(property, ((Vec4) from).lerp((Vec4) to, progress));
                }
            }
            
            for (UIStyleProperty<?> property : frame.properties()) {
                applyStyleProperty(property, frame.getRaw(property));
            }
        });
        
        UIRenderer.getInstance().addTransition(this, rawTransition);
    }
    
    // -----------------------------------------------------------------------------
    // UI Interaction
    // -----------------------------------------------------------------------------
    
    
    public UIElement focussable(boolean focussable) {
        this.focussable = focussable;
        return this;
    }
    
    public UIElement wantsPointer(boolean wantsPointer) {
        this.wantsPointer = wantsPointer;
        return this;
    }
    
    public UIElement hoverable(boolean hoverable) {
        this.hoverable = hoverable;
        return this;
    }
    
    public UIElement clickable(boolean clickable) {
        this.clickable = clickable;
        return this;
    }
    
    public boolean isFocussable() {
        return this.focussable;
    }
    
    public boolean wantsPointer() {
        return this.wantsPointer;
    }
    
    public boolean isHoverable() {
        return this.hoverable;
    }
    
    public boolean isClickable() {
        return this.clickable;
    }
    
    public void gotFocus(IOEvent event) {}
    
    public void lostFocus(IOEvent event) {}
    
    public void gotPointer(IOEvent event) {}
    
    public void lostPointer(IOEvent event) {}
    
    public void gotHover(IOEvent event) {
        if (hoverStyle==null) return;
        
        if (transitionDuration==0f) applyStyle(hoverStyle);
        else transitionToStyle(hoverStyle, new Transition(transitionDuration, "hover", null));
    }
    
    public void lostHover(IOEvent event) {
        if (transitionDuration==0f) applyStyle(baseStyle);
        else {
            Transition t = new Transition(transitionDuration, "hover", null);
            transitionToStyle(baseStyle, t);
        }
    }
    
    public void clicked(IOEvent event) {}
    
    public void handleIOEvent(IOEvent event) {}
    
    public UIElement getDeepestElementAt(float x, float y) {
        if (!contains(x, y)) return null;
        UIElement hit = null;
        
        for (UIElement c : children) {
            if (!c.isVisible()) continue;
            UIElement result = c.getDeepestElementAt(x, y);
            if (result!=null) hit = result;
        }
        
        return hit!=null ? hit : this;
    }
    
    public UIElement findAncestorMatching(Predicate<UIElement> accepts) {
        if (accepts.test(this)) return this;
        return parent!=null ? parent.findAncestorMatching(accepts) : null;
    }
    
    // -----------------------------------------------------------------------------
    // Tree Management
    // -----------------------------------------------------------------------------
    
    
    public void setParent(UIElement parent) {
        this.parent = parent;
        
        markLayoutDirty();
        markSubtreeDirty();
    }
    
    public void addChild(UIElement e) {
        children.add(e);
        e.setParent(this);
        
        markLayoutDirty();
        markSubtreeDirty();
    }
    
    public void removeChild(UIElement e) {
        children.remove(e);
        e.setParent(null);
        
        markLayoutDirty();
        markSubtreeDirty();
    }
    
    public List<UIElement> getAllChildren() {
        List<UIElement> elems = new ArrayList<>();
        
        for (UIElement e : children) {
            elems.add(e);
            elems.addAll(e.getAllChildren());
        }
        return elems;
    }
    
    public List<UIElement> getVisibleChildren() {
        List<UIElement> elems = new ArrayList<>();
        
        for (UIElement e : children) {
            if (!e.isVisible())
                continue;
            elems.add(e);
            elems.addAll(e.getVisibleChildren());
        }
        return elems;
    }
    
    public UIElement getChildAtIndex(int i) {
        if (i<0||i>children.size()-1) return null;
        return children.get(i);
    }
    
    public void markLayoutDirty() {
        layoutDirty = true;
        if (parent != null) parent.markLayoutDirty();
    }
    
    public boolean isLayoutDirty() {
        return layoutDirty;
    }
    
    public void markSubtreeDirty() {
        subtreeDirty = true;
        if (parent != null) parent.markSubtreeDirty();
    }
    
    public boolean isSubtreeDirty() {
        return subtreeDirty;
    }
    
    public void clearSubtreeDirtyMark() {
        subtreeDirty = false;
        for (UIElement c : children) c.clearSubtreeDirtyMark();
    }
    
    public void initialise() {}
    
    public boolean isInitialised() {
        return this.initialised;
    }
    
    public void initialiseSubtree() {
        if (!initialised) {
            initialise();
            initialised = true;
        }
        
        for (UIElement child : children) child.initialiseSubtree();
    }
    
    // -----------------------------------------------------------------------------
    // Layout Management
    // -----------------------------------------------------------------------------
    
    
    /**
    * Structure:
    * Elements owns authored x y width and height all in a unit as well as position
    * mode and box mode
    * Element also has cached screen x y w h (in pixels) which it uses to build model matrix
    * Element also has local x y w h (in pixels) - so relative to the parent.
    * 
    * Calculate PASS
    * calculate called from root first
    * 
    * each element calculates it's own size if it's a fixed box
    * then asks each child to measure
    * therefore every element past the start of the child loop has a measured width and height
    * 
    * then for every child in the parent, it sets the measured x and y based on authored values
    * if the child x and y is in parent width or height, this is okay if it's a fixed box as that has been set
    * but parent width and height is disabled when parent is a flex box so this is still safe.
    * 
    * then if the parent is a flex box it can calculate it's max bounds and set it's size
    * 
    * padding is tracked throughout the process, so for relative elements, their measuredx will contain padding left (tracked through cursor)
    * and their measured y will track padding top
    * 
    * flex box width and height setting will also include padding right and bottom.
    * 
    * ADVANCE PASS
    * advance called from root with it's own measured x and y passed in.
    * each element sets resolved x/y from the parameters and resolved w/h from measured w/h.
    * 
    * then it will loop over all it's children
    * if child is screen absolute then it's measured x and y (in pixels) is correct so it will advance to that child with those values
    * otherwise it will advance to that child with the current x and y + the child's x and ys
    */
    
    private static class FlowLine {
        List<UIElement> elements = new ArrayList<>();
        
        float left = Float.POSITIVE_INFINITY;
        float right = Float.NEGATIVE_INFINITY;
        float top = Float.POSITIVE_INFINITY;
        float bottom = Float.NEGATIVE_INFINITY;
        
        void add(UIElement e, float mL, float mR, float mT, float mB) {
            elements.add(e);
            
            left = Math.min(left, e.localX - mL);
            right = Math.max(right, e.localX + e.localWidth + mR);
            top = Math.min(top, e.localY - mT);
            bottom = Math.max(bottom, e.localY + e.localHeight + mB);
        }
        
        boolean isEmpty() {
            return elements.isEmpty();
        }
        
        float usedWidth() {
            return right-left;
        }
        
        float usedHeight() {
            return bottom-top;
        }
    }
    
    public void layoutCalculate() {
        float bL = resolve(borderThickness);
        float bR = resolve(borderThickness);
        float bT = resolve(borderThickness);
        float bB = resolve(borderThickness);
        
        if (!borderEnabled) {
            bL = 0f;
            bR = 0f;
            bT = 0f;
            bB = 0f;
        }
        
        float pL = resolve(paddingLeft);
        float pR = resolve(paddingRight);
        float pT = resolve(paddingTop);
        float pB = resolve(paddingBottom);
        
        float contentLeft = bL+pL;
        float contentTop = bT+pT;
        float contenttRight = pR+bR;
        float contentBottom = pB+bB;
        
        float cursorX = contentLeft;
        float cursorY = contentTop;
        float maxX = 0f;
        float maxY = 0f;
        
        List<FlowLine> flowLines = new ArrayList<>();
        FlowLine flowLine = new FlowLine();
        float lineSize = 0f;
        float availableWidth = -1f;
        float availableHeight = -1f;
        
        // Root case - set own x and y
        if (parent==null) {
            localX = resolve(marginLeft);
            localY = resolve(marginTop);
        }
        
        // Fixed box - set width, height and wrap limits
        if (boxMode==UIBoxMode.FIXED) {
            float w = resolve(width);
            float h = resolve(height);
            
            // Text special case
            if (this instanceof UIText) {
                w = ((UIText) this).getResolvedTextWidth();
                h = ((UIText) this).getResolvedTextHeight();
            }
            
            if (minWidth!=null) w = Math.max(w, resolve(minWidth));
            if (minHeight!=null) h = Math.max(h, resolve(minHeight));
            
            localWidth = w;
            localHeight = h;
            
            availableWidth = Math.max(0f, localWidth-contentLeft-contenttRight);
            availableHeight = Math.max(0f, localHeight-contentTop-contentBottom);
        }
        
        // Flex box - attempt to set wrap limits
        if (boxMode==UIBoxMode.FLEX) {
            if (maxWidth!=null) availableWidth = Math.max(0f, resolve(maxWidth)-contentLeft-contenttRight);
            if (maxHeight!=null) availableHeight = Math.max(0f, resolve(maxHeight)-contentTop-contentBottom);
        }
        
        for (UIElement c : children) {
            c.layoutCalculate();
            
            // Resolve local coords
            float mL = c.resolve(c.getMarginLeft());
            float mR = c.resolve(c.getMarginRight());
            float mT = c.resolve(c.getMarginTop());
            float mB = c.resolve(c.getMarginBottom());
            
            float oL = c.resolve(c.getOffsetLeft());
            float oR = c.resolve(c.getOffsetRight());
            float oT = c.resolve(c.getOffsetTop());
            float oB = c.resolve(c.getOffsetBottom());
            
            UIPositionMode cPosition = c.getPositionMode();
            
            // Position in flow (wrapped and no wrap)
            if (cPosition==UIPositionMode.FLOW || cPosition==UIPositionMode.FLOW_RELATIVE) {
                if (flowDirection==UIFlowDirection.ROW) {
                    if (flowWrap &&
                        availableWidth!=-1 &&
                        !flowLine.isEmpty() && // Prevent wrapping of 1st element
                        cursorX-contentLeft+mL+c.localWidth+mR>availableWidth
                    ) {
                        cursorX = contentLeft;
                        cursorY += lineSize;
                        lineSize = 0f;
                        flowLines.add(flowLine);
                        flowLine = new FlowLine();
                    }
                    
                    c.localX = cursorX+mL;
                    c.localY = cursorY+mT;
                    cursorX = c.localX+c.localWidth+mR;
                    
                    lineSize = Math.max(
                        lineSize,
                        mT+c.localHeight+mB
                    );
                }
                
                if (flowDirection==UIFlowDirection.COLUMN) {
                    if (flowWrap &&
                        availableHeight!=-1 &&
                        !flowLine.isEmpty() &&
                        cursorY-contentTop+mT+c.localHeight+mB>availableHeight
                    ) {
                        cursorX += lineSize;
                        cursorY = contentTop;
                        lineSize = 0f;
                        flowLines.add(flowLine);
                        flowLine = new FlowLine();
                    }
                    
                    c.localX = cursorX+mL;
                    c.localY = cursorY+mT;
                    cursorY = c.localY+c.localHeight+mB;
                    
                    lineSize = Math.max(
                        lineSize,
                        mL+c.localWidth+mR
                    );
                }
                flowLine.add(c, mL, mR, mT, mB);
            }
            
            // Absolute positioning
            if (cPosition==UIPositionMode.ABSOLUTE||cPosition==UIPositionMode.ABSOLUTE_FIXED) {
                c.localX = bL+oL;
                c.localY = bT+oT;
            }
            
            // Screen positioning
            if (cPosition==UIPositionMode.SCREEN) {
                c.localX = oL;
                c.localY = oT;
            }
            
            // Re-calculate max values
            if (cPosition==UIPositionMode.FLOW || cPosition==UIPositionMode.FLOW_RELATIVE) {
                maxX = Math.max(maxX, c.localX+c.localWidth);
                maxY = Math.max(maxY, c.localY+c.localHeight);
            }
        }
        if (!flowLine.isEmpty()) flowLines.add(flowLine); // Add last line if nessacary
        
        // Resize flex boxes now that child sizing is known
        if (boxMode == UIBoxMode.FLEX) {
            localWidth = maxX+pR+bR;
            localHeight = maxY+pB+bB;
            
            if (minWidth!=null) localWidth = Math.max(localWidth, resolve(minWidth));
            if (maxWidth!=null) localWidth = Math.min(localWidth, resolve(maxWidth));
            
            if (minHeight!=null) localHeight = Math.max(localHeight, resolve(minHeight));
            if (maxHeight!=null) localHeight = Math.min(localHeight, resolve(maxHeight));
        }
        
        // Justify and align
        float contentWidth = Math.max(0f, localWidth-contentLeft-contenttRight);
        float contentHeight = Math.max(0f, localHeight-contentTop-contentBottom);
        
        for (FlowLine line : flowLines) {
            float usedWidth = line.usedWidth();
            float usedHeight = line.usedHeight();
            
            if (justifyContent == UIJustifyContent.CENTER) {
                switch (flowDirection) {
                    case ROW:
                    float offset = (contentWidth - usedWidth) * 0.5f - (line.left - contentLeft);
                    for (UIElement c : line.elements) c.localX += offset;
                    line.left += offset;
                    line.right += offset;
                    break;
                    
                    case COLUMN:
                    offset = (contentHeight - usedHeight) * 0.5f - (line.top - contentTop);
                    for (UIElement c : line.elements) c.localY += offset;
                    line.top += offset;
                    line.bottom += offset;
                    break;
                }
            }
            
            if (alignItems == UIAlignItems.CENTER) {
                switch (flowDirection) {
                    case ROW:
                    float lineCrossStart = flowWrap ? line.top : contentTop;
                    float lineCrossSize = flowWrap ? line.usedHeight() : contentHeight;
                    
                    for (UIElement c : line.elements) {
                        float mT = c.resolve(c.getMarginTop());
                        float mB = c.resolve(c.getMarginBottom());
                        
                        float childMarginBoxHeight = mT + c.localHeight + mB;
                        float childCrossOffset = (lineCrossSize - childMarginBoxHeight) * 0.5f;
                        
                        c.localY = lineCrossStart + childCrossOffset + mT;
                    }
                    break;
                    
                    case COLUMN:
                    lineCrossStart = flowWrap ? line.left : contentLeft;
                    lineCrossSize = flowWrap ? line.usedWidth() : contentWidth;
                    
                    for (UIElement c : line.elements) {
                        float mL = c.resolve(c.getMarginLeft());
                        float mR = c.resolve(c.getMarginRight());
                        
                        float childMarginBoxWidth = mL + c.localWidth + mR;
                        float childCrossOffset = (lineCrossSize - childMarginBoxWidth) * 0.5f;
                        
                        c.localX = lineCrossStart + childCrossOffset + mL;
                    }
                    break;
                }
                
                line.left = Float.POSITIVE_INFINITY;
                line.right = Float.NEGATIVE_INFINITY;
                line.top = Float.POSITIVE_INFINITY;
                line.bottom = Float.NEGATIVE_INFINITY;
                
                for (UIElement c : line.elements) {
                    float mL = c.resolve(c.getMarginLeft());
                    float mR = c.resolve(c.getMarginRight());
                    float mT = c.resolve(c.getMarginTop());
                    float mB = c.resolve(c.getMarginBottom());
                    
                    line.left = Math.min(line.left, c.localX - mL);
                    line.right = Math.max(line.right, c.localX + c.localWidth + mR);
                    line.top = Math.min(line.top, c.localY - mT);
                    line.bottom = Math.max(line.bottom, c.localY + c.localHeight + mB);
                }
            }
        }
        
        if (alignContent == UIAlignContent.CENTER && flowLines.size() > 1) {
            float blockLeft = Float.POSITIVE_INFINITY;
            float blockRight = Float.NEGATIVE_INFINITY;
            float blockTop = Float.POSITIVE_INFINITY;
            float blockBottom = Float.NEGATIVE_INFINITY;
            
            for (FlowLine line : flowLines) {
                blockLeft = Math.min(blockLeft, line.left);
                blockRight = Math.max(blockRight, line.right);
                blockTop = Math.min(blockTop, line.top);
                blockBottom = Math.max(blockBottom, line.bottom);
            }
            
            float usedWidth = blockRight - blockLeft;
            float usedHeight = blockBottom - blockTop;
            
            switch (flowDirection) {
                case ROW:
                float offset = (contentHeight - usedHeight) * 0.5f - (blockTop - contentTop);
                for (FlowLine line : flowLines) {
                    for (UIElement c : line.elements) c.localY += offset;
                }
                break;
                
                case COLUMN:
                offset = (contentWidth - usedWidth) * 0.5f - (blockLeft - contentLeft);
                for (FlowLine  line : flowLines) {
                    for (UIElement c : line.elements) c.localX += offset;
                }
                break;
            }
        }
        
        // Calculate final child bounds
        UIBounds b = new UIBounds(contentLeft, contentTop, contentLeft, contentTop);
        
        for (UIElement c : children) {
            if (c.positionMode==UIPositionMode.SCREEN||c.positionMode==UIPositionMode.ABSOLUTE_FIXED) continue;
            
            // Add margins because local values refer to child border box only
            float mL = c.resolve(c.getMarginLeft());
            float mR = c.resolve(c.getMarginRight());
            float mT = c.resolve(c.getMarginTop());
            float mB = c.resolve(c.getMarginBottom());
            
            b.setMinX(Math.min(b.getMinX(), c.localX-mL));
            b.setMinY(Math.min(b.getMinY(), c.localY-mT));
            b.setMaxX(Math.max(b.getMaxX(), c.localX+c.localWidth+mR));
            b.setMaxY(Math.max(b.getMaxY(), c.localY+c.localHeight+mB));
        }
        
        childContentBounds = b;
        updateScrollBars();
    }
    
    public void layoutPlace(float screenX, float screenY, UIBounds inheritedClip) {
        // Place this element with provided values
        this.screenX = screenX;
        this.screenY = screenY;
        this.screenWidth = localWidth;
        this.screenHeight = localHeight;
        this.effectiveClip = inheritedClip;
        updateModelMatrix();
        
        // Determine children clipping bounds
        UIBounds contentBounds = getScreenContentBoxBounds();
        float minX = inheritedClip.getMinX();
        float minY = inheritedClip.getMinY();
        float maxX = inheritedClip.getMaxX();
        float maxY = inheritedClip.getMaxY();
        
        if (overflowX==UIOverflowMode.HIDDEN||overflowX==UIOverflowMode.SCROLL) {
            minX = contentBounds.getMinX();
            maxX = contentBounds.getMaxX();
        }
        if (overflowY==UIOverflowMode.HIDDEN||overflowY==UIOverflowMode.SCROLL) {
            minY = contentBounds.getMinY();
            maxY = contentBounds.getMaxY();
        }
        UIBounds childClip = inheritedClip.intersect(new UIBounds(minX, minY, maxX, maxY));
        
        // Place children
        for (UIElement c : children) {
            if (c.positionMode==UIPositionMode.SCREEN) {
                c.layoutPlace(c.localX, c.localY, childClip);
                continue;
            }
            
            // Figure out scroll offsets
            float scrollOffsetX = 0f;
            float scrollOffsetY = 0f;
            
            if (overflowX==UIOverflowMode.SCROLL) {
                float travel = Math.max(0f, childContentBounds.getWidth()-getLocalContentBoxWidth());
                scrollOffsetX = -(travel*scrollX);
            }
            if (overflowY==UIOverflowMode.SCROLL) {
                float travel = Math.max(0f, childContentBounds.getHeight()-getLocalContentBoxHeight());
                scrollOffsetY = -(travel*scrollY);
            }
            
            // Place children
            float childX = screenX+c.localX;
            float childY = screenY+c.localY;
            
            if (c.positionMode!=UIPositionMode.ABSOLUTE_FIXED) {
                childX += scrollOffsetX;
                childY += scrollOffsetY;
            }
            
            if (c.positionMode==UIPositionMode.FLOW_RELATIVE) {
                childX += c.resolve(c.getOffsetLeft());
                childY += c.resolve(c.getOffsetTop());
            }
            
            c.layoutPlace(childX, childY, childClip);
        }
        
        layoutDirty = false;
    }
    
    public float resolve(UILength pair) {
        switch (pair.unit) {
            case PIXELS:
            return pair.value;
            
            case SCREEN_WIDTH:
            return pair.value * SWMain.getUIWindow().getWidth();
            
            case SCREEN_HEIGHT:
            return pair.value * SWMain.getUIWindow().getHeight();
            
            case PARENT_BORDER_BOX_WIDTH:
            if (parent==null) throwResolveException(1);
            if (parent.getBoxMode()==UIBoxMode.FLEX) throwResolveException(2);
            
            return pair.value * parent.getLocalBorderBoxWidth();
            
            case PARENT_BORDER_BOX_HEIGHT:
            if (parent==null) throwResolveException(1);
            if (parent.getBoxMode()==UIBoxMode.FLEX) throwResolveException(2);
            
            return pair.value * parent.getLocalBorderBoxHeight();
            
            case PARENT_PADDING_BOX_WIDTH:
            if (parent==null) throwResolveException(1);
            if (parent.getBoxMode()==UIBoxMode.FLEX) throwResolveException(2);
            
            return pair.value * parent.getLocalPaddingBoxWidth();
            
            case PARENT_PADDING_BOX_HEIGHT:
            if (parent==null) throwResolveException(1);
            if (parent.getBoxMode()==UIBoxMode.FLEX) throwResolveException(2);
            
            return pair.value * parent.getLocalPaddingBoxHeight();
            
            case PARENT_CONTENT_BOX_WIDTH:
            if (parent==null) throwResolveException(1);
            if (parent.getBoxMode()==UIBoxMode.FLEX) throwResolveException(2);
            
            return pair.value * parent.getLocalContentBoxWidth();
            
            case PARENT_CONTENT_BOX_HEIGHT:
            if (parent==null) throwResolveException(1);
            if (parent.getBoxMode()==UIBoxMode.FLEX) throwResolveException(2);
            
            return pair.value * parent.getLocalContentBoxHeight();
            
            default:
            return 0f;
        }
    }
    
    private void throwResolveException(int e) {
        if (e==1) {
            Logger.throwRuntimeException("Cannot use parental units on element with no parent");
        }
        if (e==2) {
            Logger.throwRuntimeException("Cannot use parental units on element with parent in box mode flex");
        }
    }
    
    public boolean contains(float x, float y) {
        if (effectiveClip!=null && !effectiveClip.contains(x, y)) return false;
        
        float cx = screenX + screenWidth * 0.5f;
        float cy = screenY + screenHeight * 0.5f;
        
        float w = screenWidth * transformScaleX;
        float h = screenHeight * transformScaleY;
        
        return x >= cx - w * 0.5f &&
        x <= cx + w * 0.5f &&
        y >= cy - h * 0.5f &&
        y <= cy + h * 0.5f;
    }
    
    public void updateScrollBars() {
        if (overflowX==UIOverflowMode.SCROLL&&horizontalBar!=null&&childContentBounds!=null) {
            horizontalBar.setThumb(
                childContentBounds.getWidth(),
                getLocalContentBoxWidth(),
                scrollX
            );
        }
        
        if (overflowY==UIOverflowMode.SCROLL&&verticalBar!=null&&childContentBounds!=null) {
            verticalBar.setThumb(
                childContentBounds.getHeight(),
                getLocalContentBoxHeight(),
                scrollY
            );
        }
    }
    
    // Local sizes
    
    public float getLocalBorderBoxWidth() {
        return localWidth;
    }
    
    public float getLocalBorderBoxHeight() {
        return localHeight;
    }
    
    public float getLocalPaddingBoxWidth() {
        if (!borderEnabled) return getLocalBorderBoxWidth();
        return Math.max(0f, getLocalBorderBoxWidth()-resolve(borderThickness)*2);
    }
    
    public float getLocalPaddingBoxHeight() {
        if (!borderEnabled) return getLocalBorderBoxHeight();
        return Math.max(0f, getLocalBorderBoxHeight()-resolve(borderThickness)*2);
    }
    
    public float getLocalContentBoxWidth() {
        return Math.max(0f, getLocalPaddingBoxWidth()-resolve(paddingLeft)-resolve(paddingRight));
    }
    
    public float getLocalContentBoxHeight() {
        return Math.max(0f, getLocalPaddingBoxHeight()-resolve(paddingTop)-resolve(paddingBottom));
    }
    
    // Screen sizes
    
    public UIBounds getScreenBorderBoxBounds() {
        return new UIBounds(
            screenX,
            screenY,
            screenX+screenWidth,
            screenY+screenHeight
        );
    }
    
    public UIBounds getScreenPaddingBoxBounds() {
        UIBounds b = getScreenBorderBoxBounds();
        if (borderEnabled) {
            b = getScreenBorderBoxBounds().inset(
                resolve(borderThickness),
                resolve(borderThickness),
                resolve(borderThickness),
                resolve(borderThickness)
            );
        }
        return b;
    }
    
    public UIBounds getScreenContentBoxBounds() {
        return getScreenPaddingBoxBounds().inset(
            resolve(paddingLeft),
            resolve(paddingTop),
            resolve(paddingRight),
            resolve(paddingBottom)
        );
    }
    
    private void updateModelMatrix() {
        modelMatrix = new Matrix4f()
        .translate(
            screenX + screenWidth * 0.5f,
            screenY + screenHeight * 0.5f,
            0.0f
        )
        .scale(
            screenWidth*transformScaleX,
            screenHeight*transformScaleY,
            1.0f);
        }
        
        // -----------------------------------------------------------------------------
        // Setters
        // -----------------------------------------------------------------------------
        
        protected UIElement box(UIBoxMode boxMode) {
            this.boxMode = boxMode;
            markLayoutDirty();
            return this;
        }
        
        private UIElement flowDirection(UIFlowDirection flowDirection) {
            this.flowDirection = flowDirection;
            markLayoutDirty();
            return this;
        }
        
        private UIElement flowWrap(boolean flowWrap) {
            this.flowWrap = flowWrap;
            markLayoutDirty();
            return this;
        }
        
        private UIElement position(UIPositionMode positionMode) {
            this.positionMode = positionMode;
            markLayoutDirty();
            return this;
        }
        
        private UIElement justifyContent(UIJustifyContent justifyContent) {
            this.justifyContent = justifyContent;
            markLayoutDirty();
            return this;
        }
        
        private UIElement alignItems(UIAlignItems alignItems) {
            this.alignItems = alignItems;
            markLayoutDirty();
            return this;
        }
        
        private UIElement alignContent(UIAlignContent alignContent) {
            this.alignContent = alignContent;
            markLayoutDirty();
            return this;
        }
        
        private UIElement overflowX(UIOverflowMode overflowX) {
            this.overflowX = overflowX;
            
            if (overflowX==UIOverflowMode.SCROLL) {
                if (horizontalBar==null) {
                    Logger.debug("adding");
                    horizontalBar = new UIScrollBar(ScrollAxis.HORIZONTAL);
                    addChild(horizontalBar);
                }
            }
            
            markLayoutDirty();
            return this;
        }
        
        private UIElement overflowY(UIOverflowMode overflowY) {
            this.overflowY = overflowY;
            
            if (overflowY==UIOverflowMode.SCROLL) {
                if (verticalBar==null) {
                    verticalBar = new UIScrollBar(ScrollAxis.VERTICAL);
                    addChild(verticalBar);
                }
            }
            
            markLayoutDirty();
            return this;
        }
        
        private UIElement width(UILength width) {
            if (boxMode==UIBoxMode.FLEX) Logger.throwRuntimeException("Cannot set width of box in UIBoxMode Flex");
            this.width = width;
            
            markLayoutDirty();
            return this;
        }
        
        private UIElement height(UILength height) {
            if (boxMode==UIBoxMode.FLEX) Logger.throwRuntimeException("Cannot set height of box in UIBoxMode Flex");
            this.height = height;
            
            markLayoutDirty();
            return this;
        }
        
        private UIElement maxWidth(UILength maxWidth) {
            this.maxWidth = maxWidth;
            markLayoutDirty();
            return this;
        }
        
        private UIElement maxHeight(UILength maxHeight) {
            this.maxHeight = maxHeight;
            markLayoutDirty();
            return this;
        }
        
        private UIElement minWidth(UILength minWidth) {
            this.minWidth = minWidth;
            markLayoutDirty();
            return this;
        }
        
        private UIElement minHeight(UILength minHeight) {
            this.minHeight = minHeight;
            markLayoutDirty();
            return this;
        }
        
        private UIElement paddingLeft(UILength left) {
            this.paddingLeft = left;
            markLayoutDirty();
            return this;
        }
        
        private UIElement paddingRight(UILength right) {
            this.paddingRight = right;
            markLayoutDirty();
            return this;
        }
        
        private UIElement paddingTop(UILength top) {
            this.paddingTop = top;
            markLayoutDirty();
            return this;
        }
        
        private UIElement paddingBottom(UILength bottom) {
            this.paddingBottom = bottom;
            markLayoutDirty();
            return this;
        }
        
        private UIElement marginLeft(UILength left) {
            this.marginLeft = left;
            markLayoutDirty();
            return this;
        }
        
        private UIElement marginRight(UILength right) {
            this.marginRight = right;
            markLayoutDirty();
            return this;
        }
        
        private UIElement marginTop(UILength top) {
            this.marginTop = top;
            markLayoutDirty();
            return this;
        }
        
        private UIElement marginBottom(UILength bottom) {
            this.marginBottom = bottom;
            markLayoutDirty();
            return this;
        }
        
        private UIElement offsetLeft(UILength left) {
            this.offsetLeft = left;
            markLayoutDirty();
            return this;
        }
        
        private UIElement offsetRight(UILength right) {
            this.offsetRight = right;
            markLayoutDirty();
            return this;
        }
        
        private UIElement offsetTop(UILength top) {
            this.offsetTop = top;
            markLayoutDirty();
            return this;
        }
        
        private UIElement offsetBottom(UILength bottom) {
            this.offsetBottom = bottom;
            markLayoutDirty();
            return this;
        }
        
        private UIElement opacity(float opacity) {
            this.opacity = opacity;
            return this;
        }
        
        private UIElement visible(boolean visible) {
            this.visible = visible;
            markSubtreeDirty();
            return this;
        }
        
        private UIElement borderEnabled(boolean borderEnabled) {
            this.borderEnabled = borderEnabled;
            markLayoutDirty();
            return this;
        }
        
        private UIElement borderThickness(UILength borderThickness) {
            this.borderThickness = borderThickness;
            markLayoutDirty();
            return this;
        }
        
        private UIElement borderLeft(boolean borderLeft) {
            this.borderLeft = borderLeft;
            return this;
        }
        
        private UIElement borderRight(boolean borderRight) {
            this.borderRight = borderRight;
            return this;
        }
        
        private UIElement borderTop(boolean borderTop) {
            this.borderTop = borderTop;
            return this;
        }
        
        private UIElement borderBottom(boolean borderBottom) {
            this.borderBottom = borderBottom;
            return this;
        }
        
        private UIElement transitionDuration(float transitionDuration) {
            this.transitionDuration = transitionDuration;
            return this;
        }
        
        private UIElement transformScaleX(float transformScaleX) {
            this.transformScaleX = transformScaleX;
            updateModelMatrix();
            return this;
        }
        
        private UIElement transformScaleY(float transformScaleY) {
            this.transformScaleY = transformScaleY;
            updateModelMatrix();
            return this;
        }
        
        public UIElement scrollX(float scrollX) {
            this.scrollX = Math.max(0f, Math.min(scrollX, 1f));
            
            markLayoutDirty();
            return this;
        }
        
        public UIElement scrollY(float scrollY) {
            this.scrollY = Math.max(0f, Math.min(scrollY, 1f));
            
            markLayoutDirty();
            return this;
        }
        
        public UIElement enableDebugColor(boolean debugEnabled) {
            this.debugEnabled = debugEnabled;
            return this;
        }
        
        // -----------------------------------------------------------------------------
        // Getters
        // -----------------------------------------------------------------------------
        
        
        public UIBoxMode getBoxMode() {return this.boxMode;}
        
        public UIFlowDirection getFlowDirection() {return this.flowDirection;}
        
        public boolean flowWrapEnabled() {return this.flowWrap;}
        
        public UIPositionMode getPositionMode() {return this.positionMode;}
        
        public UIJustifyContent getJustifyContent() {return this.justifyContent;}
        
        public UIAlignItems getAlignItems() {return this.alignItems;}
        
        public UIAlignContent getAlignContent() {return this.alignContent;}
        
        public UIOverflowMode getOverflowX() {return this.overflowX;}
        
        public UIOverflowMode getOverflowY() {return this.overflowY;}
        
        
        public UIElement getParent() {return this.parent;}
        
        public UILength getWidth() {return this.width;}
        
        public UILength getHeight() {return this.height;}
        
        public float getLocalX() {return this.localX;}
        
        public float getLocalY() {return this.localY;}
        
        public float getLocalWidth() {return this.localWidth;}
        
        public float getLocalHeight() {return this.localHeight;}
        
        public float getScreenX() {return this.screenX;}
        
        public float getScreenY() {return this.screenY;}
        
        public float getScreenWidth() {return this.screenWidth;}
        
        public float getScreenHeight() {return this.screenHeight;}
        
        public UILength getPaddingLeft() {return this.paddingLeft;}
        
        public UILength getPaddingRight() {return this.paddingRight;}
        
        public UILength getPaddingTop() {return this.paddingTop;}
        
        public UILength getPaddingBottom() {return this.paddingBottom;}
        
        public UILength getMarginLeft() {return this.marginLeft;}
        
        public UILength getMarginRight() {return this.marginRight;}
        
        public UILength getMarginTop() {return this.marginTop;}
        
        public UILength getMarginBottom() {return this.marginBottom;}
        
        public UILength getOffsetLeft() {return this.offsetLeft;}
        
        public UILength getOffsetRight() {return this.offsetRight;}
        
        public UILength getOffsetTop() {return this.offsetTop;}
        
        public UILength getOffsetBottom() {return this.offsetBottom;}
        
        public boolean getBorderEnabled() {return this.borderEnabled;}
        
        public UILength getBorderThickness() {return this.borderThickness;}
        
        public boolean getBorderLeft() {return this.borderLeft;}
        
        public boolean getBorderRight() {return this.borderRight;}
        
        public boolean getBorderTop() {return this.borderTop;}
        
        public boolean getBorderBottom() {return this.borderBottom;}
        
        public float getOpacity() {return this.opacity;}
        
        public boolean isVisible() {return this.visible;}
        
        public Matrix4f getModelMatrix() {return this.modelMatrix;}
    }
    
    