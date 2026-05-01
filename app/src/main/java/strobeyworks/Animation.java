package strobeyworks;

import strobeyworks.logger.Logger;
import strobeyworks.utils.Vec2;

public class Animation {
    public enum AnimationForm {
        SINE,
        SQUARE,
        RAMP,
        INVRAMP
    }
    
    @FunctionalInterface
    public interface AnimationCallback {
        void implement(int i, float value);
    }
    
    public static final float TWOPI = (float) (Math.PI*2.0);
    
    private int numElems;
    private AnimationForm form;
    private AnimationCallback callback;
    private float[] phases;
    
    private float minVal;
    private float maxVal;
    private float width;
    private float phaseLow;
    private float phaseHigh;
    private float speed;
    
    public Animation(int numElems, AnimationForm form, AnimationCallback callback) {
        this.numElems = numElems;
        this.form = form;
        this.callback = callback;
        this.width = 1f;
        this.minVal = 0f;
        this.maxVal = 1f;
        this.speed = 0.01f;
        setPhase(0f, 1f);
    }
    
    public void setWidth(float width) {this.width = width;}
    
    public void setSpeed(float speed) {this.speed = speed;}
    
    public void setPhase(float phaseLow, float phaseHigh) {
        this.phaseLow = phaseLow;
        this.phaseHigh = phaseHigh;
        calculatePhases();
    }

    public void setMinMax(float minVal, float maxVal) {
        this.minVal = minVal;
        this.maxVal = maxVal;
    }

    public void shufflePhase() {shuffle(phases);}
    
    private void calculatePhases() {
        float pLow = phaseLow*TWOPI;
        float pHigh = phaseHigh*TWOPI;
        phases = new float[numElems];
        for (int i=0; i<numElems; i++) phases[i] = ((pHigh-pLow)/numElems)*i+pLow;
    }
    
    public void trigger() {
        float t = SWMain.getTotalFrameCount()*speed;
        for (int i=0; i<numElems; i++) {
            float val = minVal;
            
            switch (form) {
                case SINE :
                val = sinePulse(minVal, maxVal, phases[i], width, t);
                break;
                case INVRAMP:
                val = inverseRampPulse(minVal, maxVal, phases[i], width, t);
                break;
                case RAMP:
                val = rampPulse(minVal, maxVal, phases[i], width, t);
                break;
                case SQUARE:
                val = squarePulse(minVal, maxVal, phases[i], width, t);
                break;
                default:
                break;
            }
            if (callback!=null) callback.implement(i, val);
        }
    }
    
    public static float sinePulse(float min, float max, float phase, float width, float t) {
        float p = (t + phase) % TWOPI;
        if (p < 0.0f) p += TWOPI;
        
        width = Math.max(0.0001f, Math.min(width, 1.0f));
        
        float activeRange = TWOPI * width;
        
        if (p > activeRange) {
            return min;
        }
        
        float localPhase = p / activeRange * TWOPI;
        
        float wave = ((float) Math.sin(localPhase - Math.PI / 2.0) + 1.0f) * 0.5f;
        
        return min + (max - min) * wave;
    }
    
    public static float squarePulse(float min, float max, float phase, float width, float t) {
        float p = (t + phase) % TWOPI;
        if (p < 0.0f) p += TWOPI;
        
        width = Math.max(0.0001f, Math.min(width, 1.0f));
        float activeRange = TWOPI * width;
        
        if (p > activeRange) {
            return min;
        }
        
        return max;
    }
    
    public static float inverseRampPulse(float min, float max, float phase, float width, float t) {
        float p = (t + phase) % TWOPI;
        if (p < 0.0f) p += TWOPI;
        
        width = Math.max(0.0001f, Math.min(width, 1.0f));
        float activeRange = TWOPI * width;
        
        if (p > activeRange) {
            return min;
        }
        
        float progress = p / activeRange;
        
        return max - (max - min) * progress;
    }
    
    
    public static float rampPulse(float min, float max, float phase, float width, float t) {
        float p = (t + phase) % TWOPI;
        if (p < 0.0f) p += TWOPI;
        
        width = Math.max(0.0001f, Math.min(width, 1.0f));
        float activeRange = TWOPI * width;
        
        if (p > activeRange) {
            return min;
        }
        
        float progress = p / activeRange;
        
        return min + (max - min) * progress;
    }
    
    
    public static float evenSpreadPhase(float count, int position) {
        return (float) ((TWOPI/count)*position);
    }
    
    public static Vec2 circlePosition(float radius, float angle) {
        float x = (float) Math.cos(angle) * radius;
        float y = (float) Math.sin(angle) * radius;
        return new Vec2(x, y);
    }
    
    public static void shuffle(float[] array) {
        java.util.Random random = new java.util.Random();
        
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            
            float temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
    
}
