package strobeyworks.ui.core;

import strobeyworks.utils.Utils;
import strobeyworks.utils.Vec3;

public class UIColor {

    private float red;
    private float green;
    private float blue;

    private float hue;
    private float saturation;
    private float brightness;

    private float alpha;

    private boolean immutable = false;

    private UIColor() {}

    public static final UIColor TRANSPARENT = UIColor.rgba(0f, 0f, 0f, 0f).makeImmutable();
    public static final UIColor BLACK = UIColor.rgb(0f, 0f, 0f).makeImmutable();
    public static final UIColor WHITE = UIColor.rgb(1f, 1f, 1f).makeImmutable();
    
    public static final UIColor GRAY_008 = UIColor.rgb(0.08f, 0.08f, 0.08f).makeImmutable();
    public static final UIColor GRAY_01 = UIColor.rgb(0.1f, 0.1f, 0.1f).makeImmutable();
    public static final UIColor GRAY_02 = UIColor.rgb(0.2f, 0.2f, 0.2f).makeImmutable();
    public static final UIColor GRAY_03 = UIColor.rgb(0.3f, 0.3f, 0.3f).makeImmutable();
    public static final UIColor GRAY_04 = UIColor.rgb(0.4f, 0.4f, 0.4f).makeImmutable();
    public static final UIColor GRAY_05 = UIColor.rgb(0.5f, 0.5f, 0.5f).makeImmutable();
    
    
    public static final UIColor RED = UIColor.rgb(1f, 0f, 0f).makeImmutable();
    public static final UIColor GREEN = UIColor.rgb(0f, 1f, 0f).makeImmutable();
    public static final UIColor LIGHT_GREEN = UIColor.rgb(0f, 1f, 0.2f).makeImmutable();
    public static final UIColor CYAN = UIColor.rgb(0f, 1f, 1f).makeImmutable();
    public static final UIColor LAV = UIColor.rgb(0f, 0.5f, 1f).makeImmutable();
    public static final UIColor BLUE = UIColor.rgb(0f, 0f, 1f).makeImmutable();
    public static final UIColor PURPLE = UIColor.rgb(0.5f, 0f, 1f).makeImmutable();

    public static final UIColor BG_GREEN = UIColor.rgb(0f, 0.2f, 0.08f).makeImmutable();

    public static UIColor rgb(float red, float green, float blue) {
        UIColor col = new UIColor();
        col.red = red;
        col.green = green;
        col.blue = blue;
        col.alpha = 1f;
        col.calculateHSB();
        return col;
    }

    public static UIColor rgba(float red, float green, float blue, float alpha) {
        UIColor col = new UIColor();
        col.red = red;
        col.green = green;
        col.blue = blue;
        col.alpha = alpha;
        col.calculateHSB();
        return col;
    }

    public static UIColor hsb(float hue, float saturation, float brightness) {
        UIColor col = new UIColor();
        col.hue = hue;
        col.saturation = saturation;
        col.brightness = brightness;
        col.alpha = 1f;
        col.calculateRGB();
        return col;
    }

    public static UIColor hsba(float hue, float saturation, float brightness, float alpha) {
        UIColor col = new UIColor();
        col.hue = hue;
        col.saturation = saturation;
        col.brightness = brightness;
        col.alpha = alpha;
        col.calculateRGB();
        return col;
    }

    public UIColor(UIColor color) {
        this.red = color.red;
        this.green = color.green;
        this.blue = color.blue;
        this.alpha = color.alpha;
        calculateHSB();
    }

    private UIColor makeImmutable() {
        immutable = true;
        return this;
    }

    private void calculateHSB() {
        Vec3 hsb = Utils.rgbToHsb(red, green, blue);
        hue = hsb.x;
        saturation = hsb.y;
        brightness = hsb.z;
    }

    private void calculateRGB() {
        Vec3 rgb = Utils.hsbToRgb(hue, saturation, brightness);
        red = rgb.x;
        green = rgb.y;
        blue = rgb.z;
    }

    public UIColor setRed(float red) {
        if (immutable) return UIColor.rgba(red, green, blue, alpha);
        this.red = red;
        calculateHSB();
        return this;
    }

    public UIColor setGreen(float green) {
        if (immutable) return UIColor.rgba(red, green, blue, alpha);
        this.green = green;
        calculateHSB();
        return this;
    }

    public UIColor setBlue(float blue) {
        if (immutable) return UIColor.rgba(red, green, blue, alpha);
        this.blue = blue;
        calculateHSB();
        return this;
    }

    public UIColor setHue(float hue) {
        if (immutable) return UIColor.hsba(hue, saturation, brightness, alpha);
        this.hue = hue;
        calculateRGB();
        return this;
    }

    public UIColor setSaturation(float saturation) {
        if (immutable) return UIColor.hsba(hue, saturation, brightness, alpha);
        this.saturation = saturation;
        calculateRGB();
        return this;
    }

    public UIColor setBrightness(float brightness) {
        if (immutable) return UIColor.hsba(hue, saturation, brightness, alpha);
        this.brightness = brightness;
        calculateRGB();
        return this;
    }

    public UIColor setAlpha(float alpha) {
        if (immutable) return UIColor.rgba(red, green, blue, alpha);
        this.alpha = alpha;
        return this;
    }

    public float getRed() {return red;}

    public float getGreen() {return green;}

    public float getBlue() {return blue;}

    public float getHue() {return hue;}

    public float getSaturation() {return saturation;}

    public float getBrightness() {return brightness;}

    public float getAlpha() {return alpha;}
}
