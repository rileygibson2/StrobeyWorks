package strobeyworks.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

import strobeyworks.ui.core.UIFont;

public final class Utils {
    private Utils() {}
    
    public static float randomBetween(float min, float max) {
        return min+(float) Math.random()*(max-min);
    }
    
    public static Vec3 directionTo(Vec3 position, Vec3 referencePoint) {
        float x = referencePoint.x - position.x;
        float y = referencePoint.y - position.y;
        float z = referencePoint.z - position.z;
        
        float len = (float) Math.sqrt(x * x + y * y + z * z);
        
        if (len == 0.0f) {
            return new Vec3(0f, 0f, 0f);
        }
        
        return new Vec3(
            x / len,
            y / len,
            z / len
        );
    }
    
    public static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
    
    public static float clamp(Number low, Number high, Number v) {
        return Math.max(low.floatValue(), Math.min(high.floatValue(), v.floatValue()));
    }
    
    public static float quadratic(float v) {
        return v*v;
    }
    
    public static float cubic(float v) {
        return v*v*v;
    }
    
    public static float smoothstep(float v) {
        return v*v*(3f-2f*v);
    }
    
    public static float smoothstepAggressive(float v) {
        return v*v*v*(v*(v*6-15)+10);
    }
    
    public static float smoothFalloffAfter(float fallOff, float v) {
        fallOff = clamp01(fallOff);
        if (fallOff>=0f) return 1f;
        
        float t = (v-fallOff)/(1f-fallOff);
        t = clamp01(t);
        return smoothstep(t);
    }
    
    public static float smoothFalloffBefore(float fallOff, float v) {
        fallOff = clamp01(fallOff);
        if (fallOff<=0f) return 1f;
        
        float t = v/fallOff;
        t = clamp01(t);
        return smoothstep(t);
    }
    
    public static float roundToDp(float value, int dp) {
        float scale = (float) Math.pow(10, dp);
        return Math.round(value*scale)/scale;
    }
    
    public static boolean isWhole(float value) {
        return Math.abs(value-Math.round(value))<0.000001f;
    }
    
    public static ByteBuffer loadResourceToByteBuffer(String resourcePath) throws IOException {
        try (InputStream in = UIFont.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Font resource not found: " + resourcePath);
            }
            
            byte[] bytes = in.readAllBytes();
            
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            
            return buffer;
        }
    }
    
    public static float lerpFloat(float a, float b, float i) {
        return a+((b-a)*i);
    }
    
    public static Vec3 hsbToRgb(float h, float s, float b) {
        h = h - (float) Math.floor(h);
        s = Math.max(0f, Math.min(s, 1f));
        b = Math.max(0f, Math.min(b, 1f));
        
        float r = b;
        float g = b;
        float bl = b;
        
        if (s != 0f) {
            float scaledHue = h * 6f;
            int sector = (int) Math.floor(scaledHue);
            float fraction = scaledHue - sector;
            
            float p = b * (1f - s);
            float q = b * (1f - s * fraction);
            float t = b * (1f - s * (1f - fraction));
            
            switch (sector) {
                case 0 -> {
                    r = b; g = t; bl = p;
                }
                case 1 -> {
                    r = q; g = b; bl = p;
                }
                case 2 -> {
                    r = p; g = b; bl = t;
                }
                case 3 -> {
                    r = p; g = q; bl = b;
                }
                case 4 -> {
                    r = t; g = p; bl = b;
                }
                default -> {
                    r = b; g = p; bl = q;
                }
            }
        }
        
        return new Vec3(r, g, bl);
    }
    
    public static Vec3 rgbToHsb(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        
        float hue = 0f;
        float saturation = max == 0f ? 0f : delta / max;
        float brightness = max;
        
        if (delta != 0f) {
            if (max == r) {
                hue = ((g - b) / delta) % 6f;
            } else if (max == g) {
                hue = ((b - r) / delta) + 2f;
            } else {
                hue = ((r - g) / delta) + 4f;
            }
            
            hue /= 6f;
            
            if (hue < 0f) {
                hue += 1f;
            }
        }
        
        return new Vec3(hue, saturation, brightness);
    }
}
