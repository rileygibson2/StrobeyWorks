package strobeyworks.pipeline.input;

import com.google.gson.JsonPrimitive;

import strobeyworks.utils.BindableValue;

public class FloatConstantInput extends ConstantInput<Float> {

    public FloatConstantInput(float value) {
        super(BindableValue.of(value));
    }

    public FloatConstantInput() {
        super(BindableValue.of(0f));
    }

    @Override
    public RenderInputState getState() {
        return new RenderInputState(
            "float",
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
