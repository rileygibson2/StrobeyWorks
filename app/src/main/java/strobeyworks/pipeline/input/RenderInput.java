package strobeyworks.pipeline.input;

public abstract class RenderInput {

    private final boolean requiresCompile;

    public RenderInput(boolean requiresCompile) {
        this.requiresCompile = requiresCompile;
    }

    public boolean requiresCompile() {
        return requiresCompile;
    }

    public abstract String getString();
}
