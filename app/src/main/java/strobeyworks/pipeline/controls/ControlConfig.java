package strobeyworks.pipeline.controls;

import strobeyworks.utils.BindableValue;

public interface ControlConfig {
    String name();
    
    public record FloatControlConfig (
        String name,
        float min,
        float max,
        int precision,
        float increment,
        float defaultValue,
        boolean slider
    ) implements ControlConfig {}
    
    public record IntegerControlConfig (
        String name,
        int min,
        int max,
        int increment,
        int defaultValue
    ) implements ControlConfig {}
    
    public record BooleanControlConfig (
        String name,
        boolean defaultValue
    ) implements ControlConfig {}
    
    public record StringControlConfig (
        String name,
        String defaultValue
    ) implements ControlConfig {}
    
    public record ActionControlConfig (
        String name,
        String buttonText,
        Runnable action
    ) implements ControlConfig {}

    public record DisplayControlConfig (
        String name,
        BindableValue<?> binding
    ) implements ControlConfig {}
}

