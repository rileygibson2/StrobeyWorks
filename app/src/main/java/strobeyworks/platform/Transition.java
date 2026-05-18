package strobeyworks.platform;

import strobeyworks.SWMain;
import strobeyworks.logger.Logger;

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
    private float delay;

    private String tag;
    
    private float startTime;
    private float progress;
    
    public Transition(float duration, TransitionUpdateCallback updateAction) {
        this.duration = duration;
        this.delay = 0f;
        this.updateAction = updateAction;
        this.interrupted = false;
        this.completed = false;
        
        this.startTime = SWMain.getTotalTime();
        this.progress = 0f;
    }

    public Transition(float duration, float delay, TransitionUpdateCallback updateAction) {
        this.duration = duration;
        this.delay = delay;
        this.updateAction = updateAction;
        this.interrupted = false;
        this.completed = false;
        
        this.startTime = SWMain.getTotalTime();
        this.progress = 0f;
    }

    public Transition(float duration, String tag, TransitionUpdateCallback updateAction) {
        this.duration = duration;
        this.delay = 0f;
        this.updateAction = updateAction;
        this.tag = tag;
        this.interrupted = false;
        this.completed = false;
        
        this.startTime = SWMain.getTotalTime();
        this.progress = 0f;
    }
    
    public void update() {
        if (isInterrupted()||isComplete()) return;
        float elapsed = SWMain.getTotalTime()-startTime;
        boolean inDelay = elapsed<delay;

        if (inDelay) return;

        if (duration<=0f) progress = 1f;
        else {
            elapsed -= delay;
            progress = Math.max(0f, Math.min(1f, elapsed/duration));
        }
        
        if (updateAction!=null) updateAction.implement(progress);

        if (progress>=1f) {
            completed = true;
            if (completedAction!=null) completedAction.implement();
        }
    }

    public void setUpdatedAction(TransitionUpdateCallback updateAction) {
        this.updateAction = updateAction;
    }

    public void setInterruptAction(TransitionEventCallback interrruptedCallback) {
        this.interrruptedAction = interrruptedCallback;
    }

    public void setCompletedAction(TransitionEventCallback completedAction) {
        this.completedAction = completedAction;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean hasTag() {
        return this.tag!=null;
    }

    public String getTag() {
        return this.tag;
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
