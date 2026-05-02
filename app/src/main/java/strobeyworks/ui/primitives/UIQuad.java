package strobeyworks.ui.primitives;

public class UIQuad {
    
    public UIPair left;
    public UIPair right;
    public UIPair top;
    public UIPair bottom;

    public UIQuad(UIPair left, UIPair right, UIPair top, UIPair bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public UIQuad(UIPair all) {
        this.left = all.clone();
        this.right = all.clone();
        this.top = all.clone();
        this.bottom = all.clone();
    }
}
