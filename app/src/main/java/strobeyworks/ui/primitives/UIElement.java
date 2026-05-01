package strobeyworks.ui.primitives;

import org.joml.Matrix4f;

import strobeyworks.SWMain;
import strobeyworks.ShaderManager;
import strobeyworks.Window;
import strobeyworks.utils.Vec2;

public abstract class UIElement {
    
    private UIElement parent;
    private Matrix4f modelMatrix;
    
    private UIPair x;
    private UIPair y;
    private UIPair width;
    private UIPair height;
    
    private boolean visible;
    
    public UIElement(UIPair x, UIPair y, UIPair width, UIPair height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = true;
        updateModelMatrix();
    }
    
    public abstract void setRenderUniforms(ShaderManager sM);

    public void setParent(UIElement parent) {this.parent = parent;}
    
    public void setX(UIPair x) {
        this.x = x;
        updateModelMatrix();
    }

    public void setY(UIPair y) {
        this.y = y;
        updateModelMatrix();
    }

    public void setPosition(UIPair x, UIPair y) {
        this.x = x;
        this.y = y;
        updateModelMatrix();
    }
    
    public void setWidth(UIPair width) {
        this.width = width;
        updateModelMatrix();
    }
    
    public void setHeight(UIPair height) {
        this.height = height;
        updateModelMatrix();
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    private float resolveUIPair(UIPair pair) {
        switch (pair.getUnit()) {
            case PIXELS : return pair.getValue();
            case SCREEN_WIDTH : return pair.getValue()*SWMain.getUIWindow().getWidth();
            case SCREEN_HEIGHT : return pair.getValue()*SWMain.getUIWindow().getHeight();
            case PARENT_WIDTH :
            if (parent==null) return pair.getValue()*SWMain.getUIWindow().getWidth();
            return pair.getValue()*parent.getResolvedWidth();
            case PARENT_HEIGHT :
            if (parent==null) return pair.getValue()*SWMain.getUIWindow().getHeight();
            return pair.getValue()*parent.getResolvedHeight();
            default : 
            return 0f;
        }
    }
    
    private void updateModelMatrix() {
        float x = getResolvedX();
        float y = getResolvedY();
        float w = getResolvedWidth();
        float h = getResolvedHeight();

        modelMatrix = new Matrix4f()
        .translate(x+w*0.5f, y+h*0.5f, 0.0f)
        .scale(w, h, 1.0f);
    }
    
    public UIPair getX() {return this.x;}
    public UIPair getY() {return this.y;}
    public UIPair getWidth() {return this.width;}
    public UIPair getHeight() {return this.height;}

    public float getResolvedX() {return resolveUIPair(x);}
    public float getResolvedY() {return resolveUIPair(y);}
    public float getResolvedWidth() {return resolveUIPair(width);}
    public float getResolvedHeight() {return resolveUIPair(height);}

    public boolean isVisible() {return this.visible;}
    public Matrix4f getModelMatrix() {return this.modelMatrix;}
    public UIElement getParent() {return this.parent;}
}
