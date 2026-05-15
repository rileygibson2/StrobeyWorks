package strobeyworks.platform;

import strobeyworks.SWMain;

public class Transition {
    
    @FunctionalInterface
    public interface TransitionUpdateCallback {
        public abstract void implement(float progress);
    }

    @FunctionalInterface
    public interface TransitionEventCallback {
        public abstract void implement();
    }
    
    private TransitionUpdateCallback updateAction;
    private TransitionEventCallback interrruptedAction;
    private TransitionEventCallback completedAction;
    private boolean interrupted;
    private boolean completed;
    private float duration;
    
    private float startTime;
    private float progress;
    
    public Transition(float duration, TransitionUpdateCallback updateCallback) {
        this.duration = duration;
        this.updateAction = updateCallback;
        this.interrupted = false;
        this.completed = false;
        
        this.startTime = SWMain.getTotalTime();
        this.progress = 0f;
    }
    
    public void update() {
        if (isInterrupted()||isComplete()) return;
        
        if (duration<=0f) progress = 1f;
        else {
            float elapsed = SWMain.getTotalTime()-startTime;
            progress = Math.max(0f, Math.min(1f, elapsed/duration));
        }
        
        if (updateAction!=null) updateAction.implement(progress);

        if (progress>=1f) {
            completed = true;
            if (completedAction!=null) completedAction.implement();
        }
    }

    public void setInterrupt(TransitionEventCallback interrruptedCallback) {
        this.interrruptedAction = interrruptedCallback;
    }

    public void setCompletedAction(TransitionEventCallback completedAction) {
        this.completedAction = completedAction;
    }

    public void interrupt() {
        if (isComplete()) return;
        interrupted = true;
        if (interrruptedAction!=null) interrruptedAction.implement();
    }

    public boolean isInterrupted() {
        return interrupted;
    }
    
    public boolean isComplete() {
        return completed;
    }
}
