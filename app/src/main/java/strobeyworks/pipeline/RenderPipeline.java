package strobeyworks.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import strobeyworks.SWMain;
import strobeyworks.logger.Logger;
import strobeyworks.pipeline.RenderNode.RenderNodeState;
import strobeyworks.pipeline.configs.TextureInput;
import strobeyworks.pipeline.configs.TextureInput.TextureInputMode;
import strobeyworks.pipeline.configs.TextureInput.TextureInputState;
import strobeyworks.platform.JSONManager;
import strobeyworks.rendernodes.MaskNode;
import strobeyworks.rendernodes.PerlinNode;
import strobeyworks.utils.Vec2;
import strobeyworks.utils.Vec2I;
import strobeyworks.utils.Vec3;

public class RenderPipeline {
    
    public record RenderPipelineState(
        List<RenderNodeState> nodes,
        String outputtingNodeID
    ) {}
    
    private static RenderPipeline instance;
    
    private Set<RenderNode> allNodes;
    private List<RenderNode> compiledOrder;
    
    private RenderNode outputtingNode;
    
    private Set<RenderPipelineListener> subscribers;
    
    public static RenderPipeline getInstance() {
        if (instance==null) instance = new RenderPipeline();
        return instance;
    }
    
    private RenderPipeline() {
        allNodes = new HashSet<>();
        compiledOrder = new ArrayList<>();
        subscribers = new HashSet<>();
    }
    
    public void simulate() {
        Vec2I d = SWMain.getOutputWindow().getFramebufferDimensions();
        
        // Nodes
        PerlinNode n1 = addNode(PerlinNode.class, d);
        n1.setUIAPosition(new Vec2(0, 0));
        n1.setColorHigh(new Vec3(0f, 0f, 1f));
        
        
        PerlinNode n2 = addNode(PerlinNode.class, d);
        n2.setUIAPosition(new Vec2(140, 200));
        n2.setColorHigh(new Vec3(1f, 0f, 0f));
        
        
        RenderNode n3 = addNode(MaskNode.class, d);
        n3.setUIAPosition(new Vec2(200, 300));
        
        // Connections
        addTextureInputToNode(n3, new TextureInput(n1));
        addTextureInputToNode(n3, new TextureInput(n2, TextureInputMode.RED));
        
        setOutputtingNode(n3);
    }
    
    public void subscribe(RenderPipelineListener listener) {
        subscribers.add(listener);
    }
    
    public void unsubscribe(RenderPipelineListener listener) {
        subscribers.remove(listener);
    }
    
    public <T extends RenderNode> T addNode(Class<T> nodeClass, Vec2I dimensions) {
        T node = RenderNode.getNode(nodeClass);
        allNodes.add(node);
        if (!compile()) throw new RuntimeException("Failed to create render node: compile failed");
        
        node.setupControls();
        RenderTarget target = RenderTarget.texture(dimensions);
        node.setCustomName(node.getShortName()+(countNodesOfType(nodeClass)));
        node.setRenderTarget(target);
        node.initialise(dimensions);
        
        return node;
    }
    
    public int countNodesOfType(Class<? extends RenderNode> type) {
        return (int) allNodes.stream()
        .filter(type::isInstance)
        .count();
    }
    
    public Set<RenderNode> getAllNodes() {
        return Set.copyOf(allNodes);
    }
    
    public void setOutputtingNode(RenderNode node) {
        if (node==null) return;
        outputtingNode = node;
        SWMain.getOutputWindowRenderer().setSource(node.getRenderTarget());
        
        for (RenderPipelineListener l : subscribers) l.outputtingNodeChanged(node);
    }
    
    public RenderNode getOutputtingNode() {
        return outputtingNode;
    }
    
    public void handleNodeControlsChanged() {
        for (RenderPipelineListener l : subscribers) l.nodeControlsChanged();
    }
    
    public boolean addTextureInputToNode(RenderNode destNode, TextureInput input) {
        if (input==null||destNode==null||input.getNode()==null) return false;
        RenderNode inputNode = input.getNode();
        
        if (inputNode==destNode) return false;
        destNode.addTextureInput(input);
        
        if (!compile()) {
            throw new RuntimeException("Render pipeline would not compile");
        }
        return true;
    }
    
    public boolean disconnectNodeInput(RenderNode inputNode, RenderNode destinationNode) {
        return true;
    }
    
    private boolean compile() {
        List<RenderNode> newCompiledOrder = new ArrayList<>();
        Set<RenderNode> visiting = new HashSet<>();
        Set<RenderNode> visited = new HashSet<>();
        
        for (RenderNode n : allNodes) {
            if (!compileVisit(n, visiting, visited, newCompiledOrder)) return false;
        }
        
        compiledOrder = newCompiledOrder;
        return true;
    }
    
    private boolean compileVisit(RenderNode n, Set<RenderNode> visiting, Set<RenderNode> visited, List<RenderNode> order) {
        if (visited.contains(n)) return true;
        if (visiting.contains(n)) return false; // Loop detected
        visiting.add(n);
        
        for (RenderNode i : n.getNodeDependancies()) {
            if (!compileVisit(i, visiting, visited, order)) return false;
        }
        
        visiting.remove(n);
        visited.add(n);
        order.add(n);
        
        return true;
    }
    
    public void savePipelineToDisk() {
        Logger.info("Saving pipeline to disk");
        RenderPipelineState state = getState();
        JSONManager.savePipelineState("test1.show", state);
        
        for (RenderPipelineListener l : subscribers) l.pipelineSavedToFile("test1.show");
    }
    
    public void loadPipelineFromDisk() {
        Logger.info("Loading pipeline from disk");
        RenderPipelineState state = JSONManager.loadPipelineState("test1.show");
        
        configureFromState(state);
        for (RenderPipelineListener l : subscribers) l.pipelineLoadedFromFile("test1.show");
    }
    
    public RenderPipelineState getState() {
        List<RenderNodeState> nodeStates = new ArrayList<>();
        for (RenderNode n : allNodes) nodeStates.add(n.getState());
        
        return new RenderPipelineState(
            nodeStates,
            outputtingNode.getIDString()
        );
    }
    
    public void configureFromState(RenderPipelineState state) {
        // Clear state
        cleanup();
        allNodes = new HashSet<>();
        compiledOrder = new ArrayList<>();
        outputtingNode = null;
        
        // Create all nodes
        Vec2I dimensions = SWMain.getOutputWindow().getFramebufferDimensions();
        Map<String, RenderNode> loadedNodes = new HashMap<>();
        
        for (RenderNodeState nodeState : state.nodes()) {
            RenderNode node = RenderNode.loadFromState(nodeState);
            
            allNodes.add(node);
            node.setupControls();
            RenderTarget target = RenderTarget.texture(dimensions);
            node.setRenderTarget(target);
            node.initialise(dimensions);
            
            loadedNodes.put(nodeState.id(), node);
        }
        
        // Link texture inputs
        for (RenderNodeState nodeState : state.nodes()) {
            RenderNode destination = loadedNodes.get(nodeState.id());
            if (destination==null||nodeState.textureInputs()==null) continue;
            
            for (TextureInputState texInputState : nodeState.textureInputs()) {
                RenderNode source = loadedNodes.get(texInputState.nodeID());
                if (source==null) continue;
                
                TextureInputMode mode = TextureInputMode.UNNECESSARY;
                if (texInputState.mode() != null) mode = TextureInputMode.valueOf(texInputState.mode());
                
                addTextureInputToNode(destination, new TextureInput(source, mode));
            }
        }
        
        // Apply controls to nodes
        for (RenderNodeState nodeState : state.nodes()) {
            RenderNode node = loadedNodes.get(nodeState.id());
            if (node==null) continue;
            node.applyControlStates(nodeState.controls());
        }
        
        // Apply pipeline settings
        for (RenderNode n : allNodes) {
            if (n.hasSameID(state.outputtingNodeID)) setOutputtingNode(n);
        }
        
        // Compile
        if (!compile()) {
            throw new RuntimeException("Pipeline state could not be loaded - compile error");
        }
        
        for (RenderPipelineListener l : subscribers) l.pipelineFullyReloaded();
    }
    
    public void iterate() {
        for (RenderNode n : compiledOrder) n.update();
        for (RenderNode n : compiledOrder) n.render();
    }
    
    public void cleanup() {
        for (RenderNode n : allNodes) n.cleanup();
    }
}
