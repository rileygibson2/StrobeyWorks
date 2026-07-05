package strobeyworks.pipeline.input;

import com.google.gson.JsonPrimitive;

import strobeyworks.utils.BindableValue;

public class BooleanConstantInput extends ConstantInput<Boolean> {

    public BooleanConstantInput(boolean value) {
        super(BindableValue.of(value));
    }

    @Override
    public RenderInputState getState() {
        return new RenderInputState(
            "boolean",
            new JsonPrimitive(binding.getValue()),
            null,
            null
        );
    }

    @Override
    public String getString() {
        return binding.getValue().toString();
    }
}
