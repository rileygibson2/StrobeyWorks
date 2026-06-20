package strobeyworks.rendernodes.configs;

public record ActionControlConfig (
    String name,
    String buttonText,
    Runnable action
) implements ControlConfig {}
