package strobeyworks.pipeline.configs;

import com.google.gson.JsonPrimitive;

import strobeyworks.pipeline.RenderNode.RenderControlState;
import strobeyworks.utils.BindableValue;

public interface ControlConfig {
    String key();
    String name();
    
    RenderControlState getState();
    
    default boolean isStateful() {
        return true;
    }
    
    public record FloatControlConfig (
        String key,
        String name,
        BindableValue<Float> binding,
        float min,
        float max,
        int precision,
        float increment,
        float defaultValue,
        boolean slider
    ) implements ControlConfig {
        public RenderControlState getState() {
            return new RenderControlState(
                key,
                "float",
                new JsonPrimitive(binding().getValue())
            );
        }
    }
    
    public record IntegerControlConfig (
        String key,
        String name,
        BindableValue<Integer> binding,
        int min,
        int max,
        int increment,
        int defaultValue
    ) implements ControlConfig {
        public RenderControlState getState() {
            return new RenderControlState(
                key,
                "integer",
                new JsonPrimitive(binding().getValue())
            );
        }
    }
    
    public record BooleanControlConfig (
        String key,
        String name,
        BindableValue<Boolean> binding,
        boolean defaultValue
    ) implements ControlConfig {
        public RenderControlState getState() {
            return new RenderControlState(
                key,
                "boolean",
                new JsonPrimitive(binding().getValue())
            );
        }
    }
    
    public record StringControlConfig (
        String key,
        String name,
        BindableValue<String> binding,
        String defaultValue
    ) implements ControlConfig {
        public RenderControlState getState() {
            return new RenderControlState(
                key,
                "string",
                new JsonPrimitive(binding().getValue())
            );
        }
    }
    
    public record ActionControlConfig (
        String key,
        String name,
        String buttonText,
        Runnable action
    ) implements ControlConfig {
        @Override
        public boolean isStateful() {
            return false;
        }

        public RenderControlState getState() {
            return null;
        }
    }
}

