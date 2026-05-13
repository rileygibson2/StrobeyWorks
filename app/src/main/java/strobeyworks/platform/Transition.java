package strobeyworks.platform;

import strobeyworks.SWMain;

public class Transition {
    
    @FunctionalInterface
    public interface TransitionCallback {
        public abstract void implement(float progress);
    }
    
    private TransitionCallback transitionCallback;
    private float duration;
    
    private float startTime;
    private float progress;
    
    public Transition(float duration, TransitionCallback callback) {
        this.duration = duration;
        this.transitionCallback = callback;
        
        this.startTime = SWMain.getTotalTime();
        this.progress = 0f;
    }
    
    public void update() {
        if (complete()) return;
        
        if (duration<=0f) progress = 1f;
        else {
            float elapsed = SWMain.getTotalTime()-startTime;
            progress = Math.max(0f, Math.min(1f, elapsed/duration));
        }
        
        if (transitionCallback!=null) transitionCallback.implement(progress);
    }
    
    public boolean complete() {
        return progress>=1f;
    }
}
