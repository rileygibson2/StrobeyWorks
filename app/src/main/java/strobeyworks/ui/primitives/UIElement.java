package strobeyworks.ui.primitives;

import static strobeyworks.ui.primitives.UIPair.px;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import strobeyworks.SWMain;
import strobeyworks.ShaderManager;
import strobeyworks.logger.Logger;

public abstract class UIElement {
    
    public enum UIBoxMode {
        FIXED, // Element size stays constant
        FLEX // Element size grows and shrinks to fit content
    }
    
    public enum UIFlowMode {
        ROW, // Elements flow from left of parent box to right
        COLUMN // Elements flow from top of parent box to bottom
    }
    
    public enum UIPosMode {
        FLOW, // Element participates in normal parent flow positioning
        ABSOLUTE, // Element ignores parent flow and positions absolutely with respect to parent
        SCREEN // Element ignores parent altogather and positions absolutely with respect to screen
    }
    
    public enum UIJustifyMode {
        START, // Flow originates from left of parent box
        CENTER // Flow originates from center of parent box
    }
    
    // Tree
    private UIElement parent;
    private List<UIElement> children;
    private boolean layoutDirty; // Object layout has changed
    private boolean subtreeDirty; // Composition of subtree has changed
    
    // Authored values
    private UIBoxMode boxMode;
    private UIFlowMode flowMode;
    private boolean flowWrap;
    private UIPosMode positionMode;
    private UIJustifyMode justifyMode;
    private UIPair width;
    private UIPair height;
    
    private UIQuad padding;
    private UIQuad margin;
    
    private UIPair minWidth;
    private UIPair minHeight;
    
    private UIPair maxWidth;
    private UIPair maxHeight;
    
    private boolean visible;
    
    // Resolved self values
    private float resolvedX;
    private float resolvedY;
    private float resolvedWidth;
    private float resolvedHeight;
    
    private float measuredX;
    private float measuredY;
    private float measuredWidth;
    private float measuredHeight;
    
    // Functional
    private Matrix4f modelMatrix;
    
    public UIElement(UIPair width, UIPair height) {
        this.width = width;
        this.height = height;
        
        this.boxMode = UIBoxMode.FIXED;
        this.flowMode = UIFlowMode.ROW;
        this.flowWrap = false;
        this.positionMode = UIPosMode.FLOW;
        this.justifyMode = UIJustifyMode.START;
        
        this.padding = new UIQuad(px(0));
        this.margin = new UIQuad(px(0));
        
        this.visible = true;
        
        this.layoutDirty = true;
        this.subtreeDirty = false;
        
        children = new ArrayList<>();
        updateModelMatrix();
    }
    
    public abstract void setRenderUniforms(ShaderManager sM);
    
    /**
    * Tree Management
    */
    
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
    
    public List<UIElement> getPositionModeChildren(UIPosMode positionMode) {
        List<UIElement> elems = new ArrayList<>();
        
        for (UIElement e : children) {
            if (e.getPositionMode() != positionMode)
                continue;
            elems.add(e);
            elems.addAll(e.getVisibleChildren());
        }
        return elems;
    }
    
    public UIElement getPositionModeChildAtIndex(UIPosMode positionMode, int index) {
        int i = 0;
        for (UIElement e : children) {
            if (e.getPositionMode() != positionMode)
                continue;
            if (i == index)
                return e;
            i++;
        }
        
        return null;
    }
    
    public void markLayoutDirty() {
        layoutDirty = true;
        if (parent != null)
            parent.markLayoutDirty();
    }
    
    public boolean isLayoutDirty() {
        return layoutDirty;
    }
    
    public void markSubtreeDirty() {
        subtreeDirty = true;
        if (parent != null)
            parent.markSubtreeDirty();
    }
    
    public boolean isSubtreeDirty() {
        return subtreeDirty;
    }
    
    public void clearSubtreeDirtyMark() {
        subtreeDirty = false;
        for (UIElement c : children)
            c.clearSubtreeDirtyMark();
    }
    
    /**
    * Layout Management
    */
    
    public void setBoxMode(UIBoxMode boxMode) {
        this.boxMode = boxMode;
        markLayoutDirty();
    }
    
    public void setFlowMode(UIFlowMode flowMode) {
        this.flowMode = flowMode;
        markLayoutDirty();
    }
    
    public void enableFlowWrap(boolean flowWrap) {
        this.flowWrap = flowWrap;
        markLayoutDirty();
    }
    
    public void setPositionMode(UIPosMode positionMode) {
        this.positionMode = positionMode;
        markLayoutDirty();
    }
    
    public void setJustifyMode(UIJustifyMode justifyMode) {
        this.justifyMode = justifyMode;
        markLayoutDirty();
    }
    
    public void setMinWidth(UIPair minWidth) {
        this.minWidth = minWidth;
        markLayoutDirty();
    }
    
    public void setMinHeight(UIPair minHeight) {
        this.minHeight = minHeight;
        markLayoutDirty();
    }
    
    public void setMaxWidth(UIPair maxWidth) {
        this.maxWidth = maxWidth;
        markLayoutDirty();
    }
    
    public void setMaxHeight(UIPair maxHeight) {
        this.maxHeight = maxHeight;
        markLayoutDirty();
    }
    
    public void setPadding(UIQuad padding) {
        this.padding = padding;
        markLayoutDirty();
    }
    
    public void setMargin(UIQuad margin) {
        this.margin = margin;
        markLayoutDirty();
    }
    
    public void setMarginLeft(UIPair left) {
        this.margin.left = left;
        markLayoutDirty();
    }
    
    public void setMarginTop(UIPair top) {
        this.margin.top = top;
        markLayoutDirty();
    }
    
    public void setWidth(UIPair width) {
        if (boxMode==UIBoxMode.FLEX) Logger.throwException("Cannot set width of box in UIBoxMode Flex");
        this.width = width;
        markLayoutDirty();
    }
    
    public void setHeight(UIPair height) {
        if (boxMode==UIBoxMode.FLEX) Logger.throwException("Cannot set height of box in UIBoxMode Flex");
        this.height = height;
        markLayoutDirty();
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
        markSubtreeDirty();
    }
    
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
    
    public void layoutMeasure() {
        float pLeft = resolveLocal(padding.left);
        float pRight = resolveLocal(padding.right);
        float pTop = resolveLocal(padding.top);
        float pBottom = resolveLocal(padding.bottom);
        
        float cursorX = pLeft;
        float cursorY = pTop;
        float maxX = 0f;
        float maxY = 0f;
        
        List<List<UIElement>> flowLines = new ArrayList<>();
        List<UIElement> flowLine = new ArrayList<>();
        float lineSize = 0f;
        float availableWidth = -1f;
        float availableHeight = -1f;
        
        // Root case - set own x and y
        if (parent==null) {
            measuredX = resolveLocal(margin.left);
            measuredY = resolveLocal(margin.top);
        }
        
        //Set fixed box w and h and wrap limits
        if (boxMode==UIBoxMode.FIXED) {
            float w = resolveLocal(width);
            if (minWidth!=null) w = Math.max(w, resolveLocal(minWidth));
            measuredWidth = w;
            
            float h = resolveLocal(height);
            if (minHeight!=null) h = Math.max(h, resolveLocal(minHeight));
            measuredHeight = h;
            
            availableWidth = measuredWidth-pLeft-pRight;
            availableHeight = measuredHeight-pTop-pBottom;
        }
        
        // Attempt to set wrap limits for flex box
        if (boxMode==UIBoxMode.FLEX) {
            if (maxWidth!=null) availableWidth = resolveLocal(maxWidth)-pLeft-pRight;
            if (maxHeight!=null) availableHeight = resolveLocal(maxHeight)-pTop-pBottom;
        }
        
        for (UIElement c : children) {
            c.layoutMeasure();
            
            // Resolve local coords
            float lLeft = c.resolveLocal(c.getMargin().left);
            float lRight = c.resolveLocal(c.getMargin().right);
            float lTop = c.resolveLocal(c.getMargin().top);
            float lBottom = c.resolveLocal(c.getMargin().bottom);
            
            // Position in flow (wrapped and no wrap)
            if (c.getPositionMode()==UIPosMode.FLOW) {
                if (flowMode==UIFlowMode.ROW) {
                    if (flowWrap &&
                        availableWidth!=-1 &&
                        cursorX>pLeft &&
                        cursorX-pLeft+lLeft+c.measuredWidth>availableWidth
                    ) {
                        cursorX = pLeft;
                        cursorY += lineSize;
                        lineSize = 0f;
                        flowLines.add(flowLine);
                        flowLine = new ArrayList<>();
                    }
                    
                    c.measuredX = cursorX+lLeft;
                    c.measuredY = cursorY+lTop;
                    cursorX = c.measuredX+c.measuredWidth+lRight;
                    
                    lineSize = Math.max(
                        lineSize,
                        lTop+c.measuredHeight+lBottom
                    );
                }
                
                if (flowMode==UIFlowMode.COLUMN) {
                    if (flowWrap &&
                        availableHeight!=-1 &&
                        cursorY>pTop &&
                        cursorY-pTop+lTop+c.measuredHeight>availableHeight
                    ) {
                        cursorX += lineSize;
                        cursorY = pTop;
                        lineSize = 0f;
                        flowLines.add(flowLine);
                        flowLine = new ArrayList<>();
                    }
                    
                    c.measuredX = cursorX+lLeft;
                    c.measuredY = cursorY+lTop;
                    cursorY = c.measuredY+c.measuredHeight+lBottom;
                    
                    lineSize = Math.max(
                        lineSize,
                        lLeft+c.measuredWidth+lRight
                    );
                }
                flowLine.add(c);
            }
            
            // Other positioning
            if (c.getPositionMode()==UIPosMode.ABSOLUTE) {
                c.measuredX = pLeft+lLeft;
                c.measuredY = pTop+lTop;
            }
            
            if (c.getPositionMode()==UIPosMode.SCREEN) {
                c.measuredX = lLeft;
                c.measuredY = lTop;
            }
            
            maxX = Math.max(maxX, c.measuredX+c.measuredWidth);
            maxY = Math.max(maxY, c.measuredY+c.measuredHeight);
        }
        if (!flowLine.isEmpty()) flowLines.add(flowLine);
        
        // Resize flex boxes
        if (boxMode == UIBoxMode.FLEX) {
            measuredWidth = maxX+pRight;
            measuredHeight = maxY+pBottom;
            
            if (minWidth!=null) measuredWidth = Math.max(measuredWidth, resolveLocal(minWidth));
            if (maxWidth!=null) measuredWidth = Math.min(measuredWidth, resolveLocal(maxWidth));
            
            if (minHeight!=null) measuredHeight = Math.max(measuredHeight, resolveLocal(minHeight));
            if (maxHeight!=null) measuredHeight = Math.min(measuredHeight, resolveLocal(maxHeight));
        }
        
        // Justify elements to center (wrapped and not wrapped)
        if (justifyMode==UIJustifyMode.CENTER) {
            switch(flowMode) {
                case ROW :
                for (List<UIElement> line : flowLines) {
                    float lineLeft = Float.POSITIVE_INFINITY; // This way avoids using left margin of first element in center calc
                    float lineRight = Float.NEGATIVE_INFINITY;
                    
                    for (UIElement c : line) {
                        lineLeft = Math.min(lineLeft, c.measuredX);
                        lineRight = Math.max(lineRight, c.measuredX+c.measuredWidth);
                    }
                    
                    float usedWidth = lineRight-lineLeft;
                    float contentWidth = Math.max(0f, measuredWidth-pLeft-pRight);
                    float offset = (contentWidth-usedWidth)*0.5f-(lineLeft-pLeft);
                    
                    for (UIElement c : line) c.measuredX += offset;
                }
                break;
                
                case COLUMN :
                for (List<UIElement> line : flowLines) {
                    float lineTop = Float.POSITIVE_INFINITY;
                    float lineBottom = Float.NEGATIVE_INFINITY;
                    
                    for (UIElement c : line) {
                        lineTop = Math.min(lineTop, c.measuredY);
                        lineBottom = Math.max(lineBottom, c.measuredY+c.measuredHeight);
                    }
                    
                    float usedHeight = lineBottom-lineTop;
                    float contentHeight = Math.max(0f, measuredHeight-pTop-pBottom);
                    float offset = (contentHeight-usedHeight)*0.5f-(lineTop-pTop);
                    
                    for (UIElement c : line) c.measuredY += offset;
                }
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
            if (c.getPositionMode()==UIPosMode.SCREEN) c.layoutAdvance(c.measuredX, c.measuredY);
            else c.layoutAdvance(resolvedX+c.measuredX, resolvedY+c.measuredY);
        }
        
        layoutDirty = false;
    }
    
    private float resolveLocal(UIPair pair) {
        switch (pair.unit) {
            case PIXELS:
            return pair.value;
            
            case SCREEN_WIDTH:
            return pair.value * SWMain.getUIWindow().getWidth();
            
            case SCREEN_HEIGHT:
            return pair.value * SWMain.getUIWindow().getHeight();
            
            case PARENT_WIDTH:
            if (parent == null)
                return pair.value * SWMain.getUIWindow().getWidth();
            if (parent.getBoxMode() == UIBoxMode.FLEX)
                Logger.throwException("Cannot use parent units on parent with box mode flex");
            return pair.value * parent.getMeasuredContentWidth();
            
            case PARENT_HEIGHT:
            if (parent == null)
                return pair.value * SWMain.getUIWindow().getHeight();
            if (parent.getBoxMode() == UIBoxMode.FLEX)
                Logger.throwException("Cannot use parent units on parent with box mode flex");
            return pair.value * parent.getMeasuredContentHeight();
            
            default:
            return 0f;
        }
    }
    
    private float getMeasuredContentWidth() {
        return Math.max(0f,
            measuredWidth - resolveLocal(padding.left) - resolveLocal(padding.right)
        );
    }
    
    private float getMeasuredContentHeight() {
        return Math.max(0f,
            measuredHeight - resolveLocal(padding.top) - resolveLocal(padding.bottom)
        );
    }
    
    private void updateModelMatrix() {
        modelMatrix = new Matrix4f()
        .translate(resolvedX + resolvedWidth * 0.5f, resolvedY + resolvedHeight * 0.5f, 0.0f)
        .scale(resolvedWidth, resolvedHeight, 1.0f);
    }
    
    /**
    * Getters
    */
    
    public UIBoxMode getBoxMode() {return this.boxMode;}
    
    public UIFlowMode getFlowMode() {return this.flowMode;}
    
    public boolean flowWrapEnabled() {return this.flowWrap;}
    
    public UIPosMode getPositionMode() {return this.positionMode;}
    
    public UIJustifyMode getJustifyMode() {return this.justifyMode;}
    
    public UIElement getParent() {return this.parent;}
    
    public UIPair getWidth() {return this.width;}
    
    public UIPair getHeight() {return this.height;}
    
    public float getMeasuredX() {return this.measuredX;}
    
    public float getMeasuredY() {return this.measuredY;}
    
    public float getMeasuredWidth() {return this.measuredWidth;}
    
    public float getMeasuredHeight() {return this.measuredHeight;}
    
    public UIQuad getPadding() {return this.padding;}
    
    public UIQuad getMargin() {return this.margin;}
    
    public boolean isVisible() {return this.visible;}
    
    public Matrix4f getModelMatrix() {return this.modelMatrix;}
}
