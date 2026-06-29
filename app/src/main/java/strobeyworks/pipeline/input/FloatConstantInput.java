package strobeyworks.pipeline.input;

import strobeyworks.platform.ShaderManager;
import strobeyworks.utils.BindableValue;

public class FloatConstantInput extends ConstantInput<Float> {

    public FloatConstantInput(String uniformName, float value) {
        super(uniformName, BindableValue.of(value));
    }

    public void upload(ShaderManager sM) {
        sM.setUniformFloat(getUniformName(), getBinding().getValue());
    }

    @Override
    public String getString() {
        return binding.getValue().toString();
    }
}
