package strobeyworks.pipeline.input;

import strobeyworks.utils.BindableValue;

public class FloatConstantInput extends ConstantInput<Float> {

    public FloatConstantInput(float value) {
        super(BindableValue.of(value));
    }

    public FloatConstantInput() {
        super(BindableValue.of(0f));
    }

    @Override
    public String getString() {
        return binding.getValue().toString();
    }
}
