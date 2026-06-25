package strobeyworks.pipeline.configs;

import strobeyworks.utils.BindableValue;

public record StringControlConfig (
    String name,
    BindableValue<String> binding,
    String defaultValue
) implements ControlConfig {}
