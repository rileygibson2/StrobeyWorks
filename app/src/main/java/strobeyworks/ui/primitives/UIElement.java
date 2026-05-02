package strobeyworks.ui.primitives;

import static strobeyworks.ui.primitives.UIPair.px;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import strobeyworks.SWMain;
import strobeyworks.ShaderManager;
import strobeyworks.logger.Logger;

public abstract class UIElement {
    
    public enum UIPosMode {
        RELATIVE,
        ABSOLUTE,
        SCREEN_ABSOLUTE
    }
    
    public enum UIBoxMode {
        FIXED,
        FLEX
    }
    
    // Tree
    private UIElement parent;
    private List<UIElement> children;
    private boolean layoutDirty; // Object layout has changed
    private boolean subtreeDirty; // Composition of subtree has changed
    
    // Authored values
    private UIPosMode positionMode;
    private UIBoxMode boxMode;
    private UIPair x;
    private UIPair y;
    private UIPair width;
    private UIPair height;
    
    private UIQuad padding;
    private UIQuad margin;
    
    private UIPair minWidth;
    private UIPair minHeight;
    
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
    
    public UIElement(UIPair x, UIPair y, UIPair width, UIPair height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        this.padding = new UIQuad(px(0));
        this.margin = new UIQuad(px(0));
        
        this.visible = true;
        this.positionMode = UIPosMode.RELATIVE;
        this.boxMode = UIBoxMode.FIXED;
        
        this.layoutDirty = true;
        this.subtreeDirty = false;
        
        children = new ArrayList<>();
        updateModelMatrix();
    }
    
    public UIElement(UIPair x, UIPair y) {
        this.x = x;
        this.y = y;
        this.width = null;
        this.height = null;
        
        this.padding = new UIQuad(px(0));
        this.margin = new UIQuad(px(0));
        
        this.visible = true;
        this.positionMode = UIPosMode.RELATIVE;
        this.boxMode = UIBoxMode.FLEX;
        
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
    
    public void setPositionMode(UIPosMode positionMode) {
        this.positionMode = positionMode;
        markLayoutDirty();
    }
    
    public void setBoxMode(UIBoxMode boxMode) {
        this.boxMode = boxMode;
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
    
    public void setPadding(UIQuad padding) {
        this.padding = padding;
        markLayoutDirty();
    }
    
    public void setMargin(UIQuad margin) {
        this.margin = margin;
        markLayoutDirty();
    }
    
    public void setX(UIPair x) {
        this.x = x;
        markLayoutDirty();
    }
    
    public void setY(UIPair y) {
        this.y = y;
        markLayoutDirty();
    }
    
    public void setPosition(UIPair x, UIPair y) {
        this.x = x;
        this.y = y;
        markLayoutDirty();
    }
    
    public void setWidth(UIPair width) {
        this.width = width;
        markLayoutDirty();
    }
    
    public void setHeight(UIPair height) {
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
        float maxX = 0f;
        float maxY = 0f;
        
        if (parent==null) {
            measuredX = resolveLocal(x);
            measuredY = resolveLocal(y);
        }
        
        if (boxMode == UIBoxMode.FIXED) {
            measuredWidth = resolveLocal(width);
            measuredHeight = resolveLocal(height);
        }
        
        for (UIElement c : children) {
            c.layoutMeasure();
            
            if (c.getPositionMode()==UIPosMode.RELATIVE) {
                c.measuredX = cursorX+c.resolveLocal(c.getX());
                c.measuredY = pTop+c.resolveLocal(c.getY());
                cursorX = c.measuredX + c.measuredWidth;
            }
            
            if (c.getPositionMode()==UIPosMode.ABSOLUTE) {
                c.measuredX = pLeft+c.resolveLocal(c.getX());
                c.measuredY = pTop+c.resolveLocal(c.getY());
            }
            
            if (c.getPositionMode()==UIPosMode.SCREEN_ABSOLUTE) {
                c.measuredX = c.resolveLocal(c.getX());
                c.measuredY = c.resolveLocal(c.getY());
            }
            
            
            maxX = Math.max(maxX, c.measuredX+c.measuredWidth);
            maxY = Math.max(maxY, c.measuredY+c.measuredHeight);
        }
        
        if (boxMode == UIBoxMode.FLEX) {
            measuredWidth = maxX+pRight;
            measuredHeight = maxY+pBottom;
            
            if (minWidth!=null) {
                float minW = resolveLocal(minWidth);
                if (measuredWidth<minW) measuredWidth = minW;
            }

            if (minHeight!=null) {
                float minH = resolveLocal(minHeight);
                if (measuredHeight<minH) measuredHeight = minH;
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
            if (c.getPositionMode()==UIPosMode.SCREEN_ABSOLUTE) c.layoutAdvance(c.measuredX, c.measuredY);
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
    
    public UIPair getX() {
        return this.x;
    }
    
    public UIPair getY() {
        return this.y;
    }
    
    public UIPair getWidth() {
        return this.width;
    }
    
    public UIPair getHeight() {
        return this.height;
    }
    
    public float getMeasuredX() {
        return this.measuredX;
    }
    
    public float getMeasuredY() {
        return this.measuredY;
    }
    
    public float getMeasuredWidth() {
        return this.measuredWidth;
    }
    
    public float getMeasuredHeight() {
        return this.measuredHeight;
    }
    
    public UIQuad getPadding() {
        return this.padding;
    }
    
    public UIQuad getMargin() {
        return this.margin;
    }
    
    public boolean isVisible() {
        return this.visible;
    }
    
    public UIPosMode getPositionMode() {
        return this.positionMode;
    }
    
    public UIBoxMode getBoxMode() {
        return this.boxMode;
    }
    
    public Matrix4f getModelMatrix() {
        return this.modelMatrix;
    }
    
    public UIElement getParent() {
        return this.parent;
    }
}
