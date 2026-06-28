package strobeyworks.pipeline;

public interface RenderPipelineListener {
    void outputtingNodeChanged(RenderNode node);
    void nodeControlsChanged();

    void pipelineFullyReloaded();

    void pipelineSavedToFile(String fileName);
    void pipelineLoadedFromFile(String fileName);
}
