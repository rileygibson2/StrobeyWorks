package strobeyworks.pipeline.input;

import strobeyworks.platform.ShaderManager;

public abstract class RenderInput {

    private String uniformName;
    private final boolean requiresCompile;

    public RenderInput(String uniformName, boolean requiresCompile) {
        this.uniformName = uniformName;
        this.requiresCompile = requiresCompile;
    }

    public void setUniformName(String uniformName) {
        this.uniformName = uniformName;
    }

    public String getUniformName() {
        return uniformName;
    }

    public abstract void upload(ShaderManager sM);

    public boolean requiresCompile() {
        return requiresCompile;
    }

    public abstract String getString();
}
