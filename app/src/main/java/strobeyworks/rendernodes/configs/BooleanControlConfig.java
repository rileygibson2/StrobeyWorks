package strobeyworks.rendernodes.configs;

import strobeyworks.utils.BindableValue;

public record BooleanControlConfig (
    String name,
    BindableValue<Boolean> binding,
    boolean defaultValue
) implements ControlConfig {}
