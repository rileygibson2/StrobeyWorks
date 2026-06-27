package strobeyworks.pipeline;

public interface RenderPipelineListener {
    void outputtingNodeChanged(RenderNode node);
    void nodeControlsChanged();

    void pipelineLoaded();

    void pipelineSaved(String savedFileName);
}
