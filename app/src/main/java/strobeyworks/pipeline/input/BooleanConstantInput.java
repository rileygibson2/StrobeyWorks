package strobeyworks.pipeline.input;

import strobeyworks.utils.BindableValue;

public class BooleanConstantInput extends ConstantInput<Boolean> {

    public BooleanConstantInput(boolean value) {
        super(BindableValue.of(value));
    }

    @Override
    public String getString() {
        return binding.getValue().toString();
    }
}
