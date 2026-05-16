package strobeyworks.ui.core;

import strobeyworks.utils.Vec4;

public class UIBounds {
    
    private float minX;
    private float maxX;
    private float minY;
    private float maxY;
    
    public UIBounds(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }
    
    public UIBounds intersect(UIBounds o) {
        return new UIBounds(
            Math.max(this.minX, o.minX),
            Math.max(this.minY, o.minY),
            Math.min(this.maxX, o.maxX),
            Math.min(this.maxY, o.maxY)
        );
    }

    public UIBounds inset(float left, float top, float right, float bottom) {
        return new UIBounds(
            this.minX+left,
            this.minY+top,
            this.maxX-right,
            this.maxY-bottom
        );
    }

    public boolean contains(float x, float y) {
        return x>=minX &&
        x<=maxX &&
        y>=minY &&
        y<=maxY;
    }

    public float getWidth() {
        return this.maxX-this.minX;
    }

    public float getHeight() {
        return this.maxY-this.minY;
    }

    public Vec4 toVec4() {
        return new Vec4(minX, minY, maxX, maxY);
    }
    
    public void setMinX(float minX) {
        this.minX = minX;
    }
    
    public void setMinY(float minY) {
        this.minY = minY;
    }
    
    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }
    
    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }
    
    public float minX() {
        return this.minX;
    }
    
    public float minY() {
        return this.minY;
    }
    
    public float maxX() {
        return this.maxX;
    }
    
    public float maxY() {
        return this.maxY;
    }
}
