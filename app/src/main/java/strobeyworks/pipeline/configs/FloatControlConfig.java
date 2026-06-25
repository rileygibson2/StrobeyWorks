package strobeyworks.pipeline.configs;

import strobeyworks.utils.BindableValue;

public record FloatControlConfig (
    String name,
    BindableValue<Float> binding,
    float min,
    float max,
    int precision,
    float increment,
    float defaultValue
) implements ControlConfig {}
