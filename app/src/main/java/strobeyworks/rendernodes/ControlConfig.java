package strobeyworks.rendernodes;

import strobeyworks.utils.BindableValue;

public record ControlConfig<T> (
    String name,
    BindableValue<T> binding,
    float min,
    float max,
    int precision,
    float increment,
    float defaultValue
) {}
