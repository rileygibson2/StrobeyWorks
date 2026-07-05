package strobeyworks.pipeline.input;

import com.google.gson.JsonElement;

public abstract class RenderInput {

    public record RenderInputState(
        String type,
        JsonElement value, // Constants
        String sourceNodeID, // Texture inputs
        String sourceTextureMode // Texture inputs
    ) {}

    private final boolean requiresCompile;

    public RenderInput(boolean requiresCompile) {
        this.requiresCompile = requiresCompile;
    }

    public boolean requiresCompile() {
        return requiresCompile;
    }

    public abstract RenderInputState getState();

    public abstract String getString();
}
