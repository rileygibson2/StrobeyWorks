package strobeyworks.pipeline.input;

import strobeyworks.platform.ShaderManager;
import strobeyworks.utils.BindableValue;

public class BooleanConstantInput extends ConstantInput<Boolean> {

    public BooleanConstantInput(String uniformName, boolean value) {
        super(uniformName, BindableValue.of(value));
    }

    public void upload(ShaderManager sM) {
        sM.setUniformInt(getUniformName(), getBinding().getValue() ? 1 : 0);
    }

    @Override
    public String getString() {
        return binding.getValue().toString();
    }
}
