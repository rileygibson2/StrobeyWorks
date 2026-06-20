package strobeyworks.rendernodes.configs;

import strobeyworks.utils.BindableValue;

public record IntegerControlConfig (
    String name,
    BindableValue<Integer> binding,
    int min,
    int max,
    int increment,
    int defaultValue
) implements ControlConfig {}
