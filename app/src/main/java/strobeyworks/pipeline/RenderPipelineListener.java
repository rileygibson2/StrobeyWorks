package strobeyworks.pipeline;

public interface RenderPipelineListener {
    void outputtingNodeChanged(RenderNode node);
    void nodeInputsChanged(RenderNode node);

    void pipelineFullyReloaded();

    void pipelineSavedToFile(String fileName);
    void pipelineLoadedFromFile(String fileName);
}
