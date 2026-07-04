package strobeyworks.pipeline.input;

import strobeyworks.utils.BindableValue;

public class SelectConstantInput extends ConstantInput<Integer> {

    private final String[] options;

    public SelectConstantInput(String[] options, int value) {
        super(BindableValue.of(value));
        this.options = options;
    }

    @Override
    public String getString() {
        return binding.getValue().toString();
    }

    public String[] getOptions() {
        return options;
    }

    public String getSelectedOption() {
        int index = binding.getValue();
        if (index<0||index>=options.length) index = 0;

        return options[index];
    }
}
