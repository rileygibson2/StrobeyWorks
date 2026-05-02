package strobeyworks.ui.primitives;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import strobeyworks.SWMain;
import strobeyworks.ShaderManager;
import static strobeyworks.ui.primitives.UIPair.ph;
import static strobeyworks.ui.primitives.UIPair.pw;
import static strobeyworks.ui.primitives.UIPair.px;
import static strobeyworks.ui.primitives.UIPair.sh;
import static strobeyworks.ui.primitives.UIPair.sw;

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
    
    private boolean visible;
    
    // Resolved self values
    private float resolvedX;
    private float resolvedY;
    private float resolvedWidth;
    private float resolvedHeight;

    // Resolved content values
    private float resolvedContentX;
    private float resolvedContentY;
    private float resolvedContentWidth;
    private float resolvedContentHeight;

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
            if (!e.isVisible()) continue;
            elems.add(e);
            elems.addAll(e.getVisibleChildren());
        }
        return elems;
    }
    
    public List<UIElement> getPositionModeChildren(UIPosMode positionMode) {
        List<UIElement> elems = new ArrayList<>();
        
        for (UIElement e : children) {
            if (e.getPositionMode()!=positionMode) continue;
            elems.add(e);
            elems.addAll(e.getVisibleChildren());
        }
        return elems;
    }
    
    public UIElement getPositionModeChildAtIndex(UIPosMode positionMode, int index) {
        int i = 0;
        for (UIElement e : children) {
            if (e.getPositionMode()!=positionMode) continue;
            if (i==index) return e;
            i++;
        }
        
        return null;
    }
    
    public void markLayoutDirty() {
        layoutDirty = true;
        if (parent!=null) parent.markLayoutDirty();
    }
    
    public boolean isLayoutDirty() {return layoutDirty;}
    
    public void markSubtreeDirty() {
        subtreeDirty = true;
        if (parent!=null) parent.markSubtreeDirty();
    }
    
    public boolean isSubtreeDirty() {return subtreeDirty;}
    
    public void clearSubtreeDirtyMark() {
        subtreeDirty = false;
        for (UIElement c : children) c.clearSubtreeDirtyMark();
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
    
    private float setResolvedX(float resolvedX) {
        this.resolvedX = resolvedX;
    }

    private float setResolvedY(float resolvedY) {
        this.resolvedY = resolvedY;
    }

    private float setResolvedWidth(float resolvedWidth) {
        this.resolvedWidth = resolvedWidth;
        return this.resolvedContentWidth;
    }

    private float setResolvedHeight(float resolvedHeight) {
        this.resolvedHeight = resolvedHeight;
        return this.resolvedHeight;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
        markSubtreeDirty();
    }
    
    /**
    * Structure:
    * Child owns virtual x y width and height all in a unit as well as position mode
    * Child also has cached resolved x y w h which it uses to build model matrix
    * 
    * Parent is only person who can rebuild resolved values for a child.
    * This makes relative positioning easier.
    * Except if an element has no parent, then it resolves it's own values
    * 
    * Currently every frame re layout tree. But later - 
    * When element gets moved/changed
    * Mark dirty in element
    * Mark parent dirty -- all the way up the tree.
    * 
    * Another model : multiple dirty flags
    * self dirty
    * subtree dirty
    * 
    * when element changes it marks itself dirty and asks it's parent to mark subtree dirty
    * parent can then decide if that change meant that it needs to mark it's parent dirty too.
    * as dirty flags may not reach root this then means we may need a buffer of dirty subtree roots so updates can occur
    * this would mean if a parent has been asked by a child to mark itself dirty but it decides not to mark parent dirty
    * then it add's itself to a cache in renderer.
    */
    public void layout() {
        if (parent==null) { // Root set's own self resolved values
            setResolvedX(resolveUIPair(getX()));
            setResolvedY(resolveUIPair(getY()));
            setResolvedWidth(resolveUIPair(getWidth()));
            setResolvedHeight(resolveUIPair(getHeight()));
        }
        
        updateModelMatrix();

        // Update self content values
        float padLeft = resolveUIPair(padding.left);
        float padTop = resolveUIPair(padding.top);
        resolvedContentX = resolvedX+padLeft;
        resolvedContentY = resolvedY+padTop;
        resolvedContentWidth = Math.max(0f, resolvedWidth-padLeft-resolveUIPair(padding.right));
        resolvedContentHeight = Math.max(0f, resolvedHeight-padTop-resolveUIPair(padding.bottom));
        
        float cursorX = resolvedContentX;
        float cursorY = resolvedContentY;

        float maxX = 0f;
        float maxY = 0f;
        
        for (UIElement c : children) {
            // Size
            c.setResolvedWidth(c.resolveUIPair(c.getWidth()));
            c.setResolvedHeight(c.resolveUIPair(c.getHeight()));
            
            // Position
            if (c.getPositionMode()==UIPosMode.RELATIVE) {
                c.setResolvedX(cursorX+c.resolveUIPair(c.getX()));
                c.setResolvedY(cursorY+c.resolveUIPair(c.getY()));
                cursorX = c.getResolvedX()+c.getResolvedWidth();
            }
            
            if (c.getPositionMode()==UIPosMode.ABSOLUTE) {
                c.setResolvedX(resolvedContentX+c.resolveUIPair(c.getX()));
                c.setResolvedY(resolvedContentY+c.resolveUIPair(c.getY()));
            }
            
            if (c.getPositionMode()==UIPosMode.SCREEN_ABSOLUTE) {
                c.setResolvedX(c.resolveUIPair(c.getX()));
                c.setResolvedY(c.resolveUIPair(c.getY()));
            }
            
            c.layout();
        }
        
        layoutDirty = false;
    }
    
    private float resolveUIPair(UIPair pair) {
        switch (pair.unit) {
            case PIXELS : return pair.value;
            
            case SCREEN_WIDTH : return pair.value*SWMain.getUIWindow().getWidth();
            
            case SCREEN_HEIGHT : return pair.value*SWMain.getUIWindow().getHeight();
            
            case PARENT_WIDTH :
            if (parent==null) return pair.value*SWMain.getUIWindow().getWidth();
            return pair.value*parent.getResolvedContentWidth();
            
            case PARENT_HEIGHT :
            if (parent==null) return pair.value*SWMain.getUIWindow().getHeight();
            return pair.value*parent.getResolvedContentHeight();
            
            default : 
            return 0f;
        }
    }
    
    private void updateModelMatrix() {
        modelMatrix = new Matrix4f()
        .translate(resolvedX+resolvedWidth*0.5f, resolvedY+resolvedHeight*0.5f, 0.0f)
        .scale(resolvedWidth, resolvedHeight, 1.0f);
    }

    /** 
     * Getters
    */
    
    public UIPair getX() {return this.x;}
    public UIPair getY() {return this.y;}
    public UIPair getWidth() {return this.width;}
    public UIPair getHeight() {return this.height;}

    public float getResolvedX() {return this.resolvedX;}
    public float getResolvedY() {return this.resolvedY;}
    public float getResolvedWidth() {return this.resolvedWidth;}
    public float getResolvedHeight() {return this.resolvedHeight;}

    public float getResolvedContentX() {return this.resolvedContentX;}
    public float getResolvedContentY() {return this.resolvedContentY;}
    public float getResolvedContentWidth() {return this.resolvedContentWidth;}
    public float getResolvedContentHeight() {return this.resolvedContentHeight;}

    public UIQuad getPadding() {return this.padding;}
    public UIQuad getMargin() {return this.margin;}
    
    public boolean isVisible() {return this.visible;}
    public UIPosMode getPositionMode() {return this.positionMode;}
    public UIBoxMode getBoxMode() {return this.boxMode;}
    public Matrix4f getModelMatrix() {return this.modelMatrix;}
    public UIElement getParent() {return this.parent;}
}
