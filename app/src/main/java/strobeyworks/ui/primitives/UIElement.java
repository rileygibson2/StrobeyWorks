package strobeyworks.ui.primitives;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.px;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.joml.Matrix4f;

import strobeyworks.SWMain;
import strobeyworks.logger.Logger;
import strobeyworks.platform.IOEvent;
import strobeyworks.platform.ShaderManager;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.utils.Vec4;

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
        * Element participates in it's parents flow within parent's content box.
        * Element margin and parent padding affects layout.
        * Element offset ignored.
        */
        FLOW,
        /**
        * Element participates in normal parent flow but can be additionally positioned with an offset.
        * Element margin and parent padding affects layout.
        * Element offset affects element final position but does not affect sibling layout.
        */
        FLOW_RELATIVE,
        /**
        * Element removed from parent flow and positioned relative to parent content box.
        * Element margin ignored.
        * Element offset affects final position.
        */
        ABSOLUTE,
        /**
        * Element removed from parent flow and positioned relative to screen bounds.
        * Element margin ignored.
        * Element offset affects final position.
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
    
    // Authored values
    private UIBoxMode boxMode;
    private UIFlowDirection flowDirection;
    private boolean flowWrap;
    private UIPositionMode positionMode;
    private UIJustifyContent justifyContent;
    private UIAlignItems alignItems;
    private UIAlignContent alignContent;
    
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
    
    private UILength minWidth;
    private UILength minHeight;
    
    private UILength maxWidth;
    private UILength maxHeight;
    
    private boolean visible;
    
    // Computed values
    private float resolvedX;
    private float resolvedY;
    private float resolvedWidth;
    private float resolvedHeight;
    
    private float measuredX;
    private float measuredY;
    private float measuredWidth;
    private float measuredHeight;
    
    // IO
    private boolean focussable;
    private boolean wantsPointer;
    private boolean hoverable;
    private boolean clickable;

    // Additional styles
    private UIStyle cachedStyle;
    private UIStyle hoverStyle;
    
    // Functional
    private Matrix4f modelMatrix;
    
    private Vec4 debugColor = col(UIColors.RED);
    private boolean debugEnabled;
    
    public UIElement(UILength width, UILength height) {
        this.width = width;
        this.height = height;
        
        this.boxMode = UIBoxMode.FIXED;
        this.flowDirection = UIFlowDirection.ROW;
        this.flowWrap = false;
        this.positionMode = UIPositionMode.FLOW;
        this.justifyContent = UIJustifyContent.START;
        this.alignItems = UIAlignItems.START;
        this.alignContent = UIAlignContent.START;
        
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
        
        this.visible = true;
        
        this.layoutDirty = true;
        this.subtreeDirty = false;
        
        this.focussable = false;
        this.wantsPointer = false;
        this.hoverable = false;
        this.clickable = false;
        
        this.debugEnabled = false;
        
        children = new ArrayList<>();
        updateModelMatrix();
    }
    
    public UIElement() {
        this.boxMode = UIBoxMode.FLEX;
        this.flowDirection = UIFlowDirection.ROW;
        this.flowWrap = false;
        this.positionMode = UIPositionMode.FLOW;
        this.justifyContent = UIJustifyContent.START;
        this.alignItems = UIAlignItems.START;
        this.alignContent = UIAlignContent.START;
        
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
        
        this.visible = true;
        
        this.layoutDirty = true;
        this.subtreeDirty = false;
        
        this.focussable = false;
        this.wantsPointer = false;
        this.hoverable = false;
        this.clickable = false;
        
        this.debugEnabled = false;
        
        children = new ArrayList<>();
        updateModelMatrix();
    }
    
    public void setRenderUniforms(ShaderManager sM) {
        sM.setUniformVec4("uDebugColor", debugColor);
        sM.setUniformInt("uDebugEnabled", debugEnabled ? 1 : 0);
    }
    
    // -----------------------------------------------------------------------------
    // Styling
    // -----------------------------------------------------------------------------
    
    public void applyStyle(UIStyle style) {}

    public UIStyle captureStyle() {
        return new UIStyle();
    }

    public void cacheStyle() {
        cachedStyle = captureStyle();
    }

    public UIElement hoverStyle(UIStyle hoverStyle) {
        this.hoverStyle = hoverStyle;
        return this;
    }

    public void applyHoverStyle() {
        if (hoverStyle!=null) applyStyle(hoverStyle);
    }

    public void applyCachedStyle() {
        if (cachedStyle!=null) applyStyle(cachedStyle);
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
    
    public void gotHover(IOEvent event) {}
    
    public void lostHover(IOEvent event) {}
    
    public void clicked(IOEvent event) {}
    
    public void handleIOEvent(IOEvent event) {}
    
    public UIElement getDeepestElementAt(float x, float y) {
        if (!containsResolved(x, y)) return null;
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
    * Element also has cached resolved x y w h (in pixels) which it uses to build model matrix
    * Element also has measured x y w h (in pixels) in local space, so relative to the parent.
    * 
    * MEASURE PASS
    * measure called from root first
    * 
    * each element measures it's own size if it's a fixed box
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
    
    private static class UILine {
        List<UIElement> elements = new ArrayList<>();
        
        float left = Float.POSITIVE_INFINITY;
        float right = Float.NEGATIVE_INFINITY;
        float top = Float.POSITIVE_INFINITY;
        float bottom = Float.NEGATIVE_INFINITY;
        
        void add(UIElement e, float mL, float mR, float mT, float mB) {
            elements.add(e);
            
            left = Math.min(left, e.measuredX - mL);
            right = Math.max(right, e.measuredX + e.measuredWidth + mR);
            top = Math.min(top, e.measuredY - mT);
            bottom = Math.max(bottom, e.measuredY + e.measuredHeight + mB);
        }
        
        boolean isEmpty() {
            return elements.isEmpty();
        }
        
        float usedWidth() {
            return right - left;
        }
        
        float usedHeight() {
            return bottom - top;
        }
    }
    
    
    public void layoutMeasure() {
        float pL = resolveLocal(paddingLeft);
        float pR = resolveLocal(paddingRight);
        float pT = resolveLocal(paddingTop);
        float pB = resolveLocal(paddingBottom);
        
        float cursorX = pL;
        float cursorY = pT;
        float maxX = 0f;
        float maxY = 0f;
        
        List<UILine> flowLines = new ArrayList<>();
        UILine flowLine = new UILine();
        float lineSize = 0f;
        float availableWidth = -1f;
        float availableHeight = -1f;
        
        // Root case - set own x and y
        if (parent==null) {
            measuredX = resolveLocal(marginLeft);
            measuredY = resolveLocal(marginTop);
        }
        
        //Set fixed box w and h and wrap limits
        if (boxMode==UIBoxMode.FIXED) {
            float w = resolveLocal(width);
            float h = resolveLocal(height);
            
            // Text special case
            if (this instanceof UIText) {
                w = ((UIText) this).getResolvedTextWidth();
                h = ((UIText) this).getResolvedTextHeight();
            }
            
            if (minWidth!=null) w = Math.max(w, resolveLocal(minWidth));
            measuredWidth = w;
            if (minHeight!=null) h = Math.max(h, resolveLocal(minHeight));
            measuredHeight = h;
            
            availableWidth = measuredWidth-pL-pR;
            availableHeight = measuredHeight-pT-pB;
        }
        
        // Attempt to set wrap limits for flex box
        if (boxMode==UIBoxMode.FLEX) {
            if (maxWidth!=null) availableWidth = resolveLocal(maxWidth)-pL-pR;
            if (maxHeight!=null) availableHeight = resolveLocal(maxHeight)-pT-pB;
        }
        
        for (UIElement c : children) {
            c.layoutMeasure();
            
            // Resolve local coords
            float mL = c.resolveLocal(c.getMarginLeft());
            float mR = c.resolveLocal(c.getMarginRight());
            float mT = c.resolveLocal(c.getMarginTop());
            float mB = c.resolveLocal(c.getMarginBottom());
            
            float oL = c.resolveLocal(c.getOffsetLeft());
            float oR = c.resolveLocal(c.getOffsetRight());
            float oT = c.resolveLocal(c.getOffsetTop());
            float oB = c.resolveLocal(c.getOffsetBottom());
            
            UIPositionMode cPosition = c.getPositionMode();
            
            // Position in flow (wrapped and no wrap)
            if (cPosition==UIPositionMode.FLOW || cPosition==UIPositionMode.FLOW_RELATIVE) {
                if (flowDirection==UIFlowDirection.ROW) {
                    if (flowWrap &&
                        availableWidth!=-1 &&
                        cursorX>pL &&
                        cursorX-pL+mL+c.measuredWidth>availableWidth
                    ) {
                        cursorX = pL;
                        cursorY += lineSize;
                        lineSize = 0f;
                        flowLines.add(flowLine);
                        flowLine = new UILine();
                    }
                    
                    c.measuredX = cursorX+mL;
                    c.measuredY = cursorY+mT;
                    cursorX = c.measuredX+c.measuredWidth+mR;
                    
                    lineSize = Math.max(
                        lineSize,
                        mT+c.measuredHeight+mB
                    );
                }
                
                if (flowDirection==UIFlowDirection.COLUMN) {
                    if (flowWrap &&
                        availableHeight!=-1 &&
                        cursorY>pT &&
                        cursorY-pT+mT+c.measuredHeight>availableHeight
                    ) {
                        cursorX += lineSize;
                        cursorY = pT;
                        lineSize = 0f;
                        flowLines.add(flowLine);
                        flowLine = new UILine();
                    }
                    
                    c.measuredX = cursorX+mL;
                    c.measuredY = cursorY+mT;
                    cursorY = c.measuredY+c.measuredHeight+mB;
                    
                    lineSize = Math.max(
                        lineSize,
                        mL+c.measuredWidth+mR
                    );
                }
                flowLine.add(c, mL, mR, mT, mB);
            }
            
            // Other positioning
            if (cPosition==UIPositionMode.ABSOLUTE || cPosition==UIPositionMode.SCREEN) {
                c.measuredX = oL;
                c.measuredY = oT;
            }
            
            if (cPosition==UIPositionMode.FLOW || cPosition==UIPositionMode.FLOW_RELATIVE) {
                maxX = Math.max(maxX, c.measuredX+c.measuredWidth);
                maxY = Math.max(maxY, c.measuredY+c.measuredHeight);
            }
        }
        if (!flowLine.isEmpty()) flowLines.add(flowLine);
        
        // Resize flex boxes
        if (boxMode == UIBoxMode.FLEX) {
            measuredWidth = maxX+pR;
            measuredHeight = maxY+pB;
            
            if (minWidth!=null) measuredWidth = Math.max(measuredWidth, resolveLocal(minWidth));
            if (maxWidth!=null) measuredWidth = Math.min(measuredWidth, resolveLocal(maxWidth));
            
            if (minHeight!=null) measuredHeight = Math.max(measuredHeight, resolveLocal(minHeight));
            if (maxHeight!=null) measuredHeight = Math.min(measuredHeight, resolveLocal(maxHeight));
        }
        
        // Justify and align
        float contentWidth = Math.max(0f, measuredWidth - pL - pR);
        float contentHeight = Math.max(0f, measuredHeight - pT - pB);
        
        for (UILine line : flowLines) {
            float usedWidth = line.usedWidth();
            float usedHeight = line.usedHeight();
            
            if (justifyContent == UIJustifyContent.CENTER) {
                switch (flowDirection) {
                    case ROW:
                    float offset = (contentWidth - usedWidth) * 0.5f - (line.left - pL);
                    for (UIElement c : line.elements) c.measuredX += offset;
                    line.left += offset;
                    line.right += offset;
                    break;
                    
                    case COLUMN:
                    offset = (contentHeight - usedHeight) * 0.5f - (line.top - pT);
                    for (UIElement c : line.elements) c.measuredY += offset;
                    line.top += offset;
                    line.bottom += offset;
                    break;
                }
            }
            
            if (alignItems == UIAlignItems.CENTER) {
                switch (flowDirection) {
                    case ROW:
                    float lineCrossStart = flowWrap ? line.top : pT;
                    float lineCrossSize = flowWrap ? line.usedHeight() : contentHeight;
                    
                    for (UIElement c : line.elements) {
                        float mT = c.resolveLocal(c.getMarginTop());
                        float mB = c.resolveLocal(c.getMarginBottom());
                        
                        float childMarginBoxHeight = mT + c.measuredHeight + mB;
                        float childCrossOffset = (lineCrossSize - childMarginBoxHeight) * 0.5f;
                        
                        c.measuredY = lineCrossStart + childCrossOffset + mT;
                    }
                    break;
                    
                    case COLUMN:
                    lineCrossStart = flowWrap ? line.left : pL;
                    lineCrossSize = flowWrap ? line.usedWidth() : contentWidth;
                    
                    for (UIElement c : line.elements) {
                        float mL = c.resolveLocal(c.getMarginLeft());
                        float mR = c.resolveLocal(c.getMarginRight());
                        
                        float childMarginBoxWidth = mL + c.measuredWidth + mR;
                        float childCrossOffset = (lineCrossSize - childMarginBoxWidth) * 0.5f;
                        
                        c.measuredX = lineCrossStart + childCrossOffset + mL;
                    }
                    break;
                }
                
                line.left = Float.POSITIVE_INFINITY;
                line.right = Float.NEGATIVE_INFINITY;
                line.top = Float.POSITIVE_INFINITY;
                line.bottom = Float.NEGATIVE_INFINITY;
                
                for (UIElement c : line.elements) {
                    float mL = c.resolveLocal(c.getMarginLeft());
                    float mR = c.resolveLocal(c.getMarginRight());
                    float mT = c.resolveLocal(c.getMarginTop());
                    float mB = c.resolveLocal(c.getMarginBottom());
                    
                    line.left = Math.min(line.left, c.measuredX - mL);
                    line.right = Math.max(line.right, c.measuredX + c.measuredWidth + mR);
                    line.top = Math.min(line.top, c.measuredY - mT);
                    line.bottom = Math.max(line.bottom, c.measuredY + c.measuredHeight + mB);
                }
            }
        }
        
        if (alignContent == UIAlignContent.CENTER && flowLines.size() > 1) {
            float blockLeft = Float.POSITIVE_INFINITY;
            float blockRight = Float.NEGATIVE_INFINITY;
            float blockTop = Float.POSITIVE_INFINITY;
            float blockBottom = Float.NEGATIVE_INFINITY;
            
            for (UILine line : flowLines) {
                blockLeft = Math.min(blockLeft, line.left);
                blockRight = Math.max(blockRight, line.right);
                blockTop = Math.min(blockTop, line.top);
                blockBottom = Math.max(blockBottom, line.bottom);
            }
            
            float usedWidth = blockRight - blockLeft;
            float usedHeight = blockBottom - blockTop;
            
            switch (flowDirection) {
                case ROW:
                float offset = (contentHeight - usedHeight) * 0.5f - (blockTop - pT);
                for (UILine line : flowLines) {
                    for (UIElement c : line.elements) c.measuredY += offset;
                }
                break;
                
                case COLUMN:
                offset = (contentWidth - usedWidth) * 0.5f - (blockLeft - pL);
                for (UILine line : flowLines) {
                    for (UIElement c : line.elements) c.measuredX += offset;
                }
                break;
            }
        }
    }
    
    
    public void layoutAdvance(float resolvedX, float resolvedY) {
        this.resolvedX = resolvedX;
        this.resolvedY = resolvedY;
        this.resolvedWidth = measuredWidth;
        this.resolvedHeight = measuredHeight;
        
        updateModelMatrix();
        
        for (UIElement c : children) {
            if (c.getPositionMode()==UIPositionMode.SCREEN) {
                c.layoutAdvance(c.measuredX, c.measuredY);
            }
            else if (c.getPositionMode()==UIPositionMode.FLOW_RELATIVE) {
                c.layoutAdvance(
                    resolvedX+c.measuredX+c.resolveLocal(c.getOffsetLeft()),
                    resolvedY+c.measuredY+c.resolveLocal(c.getOffsetTop())
                );
            }
            else c.layoutAdvance(
                resolvedX+c.measuredX,
                resolvedY+c.measuredY
            );
        }
        
        layoutDirty = false;
    }
    
    public float resolveLocal(UILength pair) {
        switch (pair.unit) {
            case PIXELS:
            return pair.value;
            
            case SCREEN_WIDTH:
            return pair.value * SWMain.getUIWindow().getWidth();
            
            case SCREEN_HEIGHT:
            return pair.value * SWMain.getUIWindow().getHeight();
            
            case PARENT_CONTENT_WIDTH:
            if (parent == null)
                return pair.value * SWMain.getUIWindow().getWidth();
            if (parent.getBoxMode() == UIBoxMode.FLEX)
                Logger.throwRuntimeException("Cannot use parental units on parent with box mode flex");
            return pair.value * parent.getMeasuredContentWidth();
            
            case PARENT_CONTENT_HEIGHT:
            if (parent == null)
                return pair.value * SWMain.getUIWindow().getHeight();
            if (parent.getBoxMode() == UIBoxMode.FLEX)
                Logger.throwRuntimeException("Cannot use parental units on parent with box mode flex");
            return pair.value * parent.getMeasuredContentHeight();
            
            case PARENT_BOX_WIDTH:
            if (parent == null)
                return pair.value * SWMain.getUIWindow().getWidth();
            if (parent.getBoxMode() == UIBoxMode.FLEX)
                Logger.throwRuntimeException("Cannot use parental units on parent with box mode flex");
            return pair.value * parent.getMeasuredWidth();
            
            case PARENT_BOX_HEIGHT:
            if (parent == null)
                return pair.value * SWMain.getUIWindow().getHeight();
            if (parent.getBoxMode() == UIBoxMode.FLEX)
                Logger.throwRuntimeException("Cannot use parental units on parent with box mode flex");
            return pair.value * parent.getMeasuredHeight();
            
            default:
            return 0f;
        }
    }
    
    public boolean containsResolved(float x, float y) {
        return x>=resolvedX &&
        x<=resolvedX+resolvedWidth &&
        y>=resolvedY &&
        y<=resolvedY+resolvedHeight;
    }
    
    private float getMeasuredContentWidth() {
        return Math.max(0f,
            measuredWidth - resolveLocal(paddingLeft) - resolveLocal(paddingRight)
        );
    }
    
    private float getMeasuredContentHeight() {
        return Math.max(0f,
            measuredHeight - resolveLocal(paddingTop) - resolveLocal(paddingBottom)
        );
    }
    
    private void updateModelMatrix() {
        modelMatrix = new Matrix4f()
        .translate(resolvedX + resolvedWidth * 0.5f, resolvedY + resolvedHeight * 0.5f, 0.0f)
        .scale(resolvedWidth, resolvedHeight, 1.0f);
    }
    
    // -----------------------------------------------------------------------------
    // Setters
    // -----------------------------------------------------------------------------
    
    
    public UIElement box(UIBoxMode boxMode) {
        this.boxMode = boxMode;
        markLayoutDirty();
        return this;
    }
    
    public UIElement flowDirection(UIFlowDirection flowDirection) {
        this.flowDirection = flowDirection;
        markLayoutDirty();
        return this;
    }
    
    public UIElement flowWrap(boolean flowWrap) {
        this.flowWrap = flowWrap;
        markLayoutDirty();
        return this;
    }
    
    public UIElement position(UIPositionMode positionMode) {
        this.positionMode = positionMode;
        markLayoutDirty();
        return this;
    }
    
    public UIElement justifyContent(UIJustifyContent justifyContent) {
        this.justifyContent = justifyContent;
        markLayoutDirty();
        return this;
    }
    
    public UIElement alignItems(UIAlignItems alignItems) {
        this.alignItems = alignItems;
        markLayoutDirty();
        return this;
    }
    
    public UIElement alignContent(UIAlignContent alignContent) {
        this.alignContent = alignContent;
        markLayoutDirty();
        return this;
    }
    
    public UIElement minWidth(UILength minWidth) {
        this.minWidth = minWidth;
        markLayoutDirty();
        return this;
    }
    
    public UIElement minHeight(UILength minHeight) {
        this.minHeight = minHeight;
        markLayoutDirty();
        return this;
    }
    
    public UIElement maxWidth(UILength maxWidth) {
        this.maxWidth = maxWidth;
        markLayoutDirty();
        return this;
    }
    
    public UIElement maxHeight(UILength maxHeight) {
        this.maxHeight = maxHeight;
        markLayoutDirty();
        return this;
    }
    
    public UIElement padding(UILength padding) {
        this.paddingLeft = padding.clone();
        this.paddingRight = padding.clone();
        this.paddingTop = padding.clone();
        this.paddingBottom = padding.clone();
        
        markLayoutDirty();
        return this;
    }
    
    public UIElement paddingLeft(UILength left) {
        this.paddingLeft = left;
        markLayoutDirty();
        return this;
    }
    
    public UIElement paddingRight(UILength right) {
        this.paddingRight = right;
        markLayoutDirty();
        return this;
    }
    
    public UIElement paddingTop(UILength top) {
        this.paddingTop = top;
        markLayoutDirty();
        return this;
    }
    
    public UIElement paddingBottom(UILength bottom) {
        this.paddingBottom = bottom;
        markLayoutDirty();
        return this;
    }
    
    public UIElement margin(UILength margin) {
        this.marginLeft = margin.clone();
        this.marginRight = margin.clone();
        this.marginTop = margin.clone();
        this.marginBottom = margin.clone();
        
        markLayoutDirty();
        return this;
    }
    
    public UIElement marginLeft(UILength left) {
        this.marginLeft = left;
        markLayoutDirty();
        return this;
    }
    
    public UIElement marginRight(UILength right) {
        this.marginRight = right;
        markLayoutDirty();
        return this;
    }
    
    public UIElement marginTop(UILength top) {
        this.marginTop = top;
        markLayoutDirty();
        return this;
    }
    
    public UIElement marginBottom(UILength bottom) {
        this.marginBottom = bottom;
        markLayoutDirty();
        return this;
    }
    
    public UIElement offset(UILength offset) {
        this.offsetLeft = offset.clone();
        this.offsetRight = offset.clone();
        this.offsetTop = offset.clone();
        this.offsetBottom = offset.clone();
        
        markLayoutDirty();
        return this;
    }
    
    public UIElement offsetLeft(UILength left) {
        this.offsetLeft = left;
        markLayoutDirty();
        return this;
    }
    
    public UIElement offsetRight(UILength right) {
        this.offsetRight = right;
        markLayoutDirty();
        return this;
    }
    
    public UIElement offsetTop(UILength top) {
        this.offsetTop = top;
        markLayoutDirty();
        return this;
    }
    
    public UIElement offsetBottom(UILength bottom) {
        this.offsetBottom = bottom;
        markLayoutDirty();
        return this;
    }
    
    public UIElement width(UILength width) {
        if (boxMode==UIBoxMode.FLEX) Logger.throwRuntimeException("Cannot set width of box in UIBoxMode Flex");
        this.width = width;
        markLayoutDirty();
        return this;
    }
    
    public UIElement height(UILength height) {
        if (boxMode==UIBoxMode.FLEX) Logger.throwRuntimeException("Cannot set height of box in UIBoxMode Flex");
        this.height = height;
        markLayoutDirty();
        return this;
    }
    
    public UIElement visible(boolean visible) {
        this.visible = visible;
        markSubtreeDirty();
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
    
    public UIElement getParent() {return this.parent;}
    
    public UILength getWidth() {return this.width;}
    
    public UILength getHeight() {return this.height;}
    
    public float getMeasuredX() {return this.measuredX;}
    
    public float getMeasuredY() {return this.measuredY;}
    
    public float getMeasuredWidth() {return this.measuredWidth;}
    
    public float getMeasuredHeight() {return this.measuredHeight;}
    
    public float getResolvedX() {return this.resolvedX;}
    
    public float getResolvedY() {return this.resolvedY;}
    
    public float getResolvedWidth() {return this.resolvedWidth;}
    
    public float getResolvedHeight() {return this.resolvedHeight;}
    
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
    
    public boolean isVisible() {return this.visible;}
    
    public Matrix4f getModelMatrix() {return this.modelMatrix;}
}
