package strobeyworks.noiserender;

import strobeyworks.utils.Bindable;

public record ControlConfig<T> (
    String name,
    Bindable<T> binding,
    float min,
    float max,
    int precision,
    float increment,
    float defaultValue
) {}
