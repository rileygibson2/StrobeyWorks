package strobeyworks.pipeline.input;

import strobeyworks.utils.BindableValue;

public abstract class ConstantInput<T> extends RenderInput {

    protected BindableValue<T> binding;

	public ConstantInput(BindableValue<T> binding) {
		super(false);
        this.binding = binding;
	}

    public BindableValue<T> getBinding() {
        return binding;
    }
}
