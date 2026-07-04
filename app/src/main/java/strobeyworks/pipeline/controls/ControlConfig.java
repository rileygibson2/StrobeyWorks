package strobeyworks.pipeline.controls;

public interface ControlConfig<T> {
    String name();
    
    public record FloatControlConfig (
        String name,
        float min,
        float max,
        int precision,
        float increment,
        float defaultValue,
        boolean slider
    ) implements ControlConfig<Float> {}
    
    public record IntegerControlConfig (
        String name,
        int min,
        int max,
        int increment,
        int defaultValue
    ) implements ControlConfig<Integer> {}
    
    public record BooleanControlConfig (
        String name,
        boolean defaultValue
    ) implements ControlConfig<Boolean> {}
    
    public record StringControlConfig (
        String name,
        String defaultValue
    ) implements ControlConfig<String> {}

    public record SelectControlConfig (
        String name,
        String[] options,
        int defaultValue
    ) implements ControlConfig<String> {}
    
    public record ActionControlConfig (
        String name,
        String buttonText,
        Runnable action
    ) implements ControlConfig<Void> {}

    public record DisplayControlConfig (
        String name
    ) implements ControlConfig<Object> {}
}

