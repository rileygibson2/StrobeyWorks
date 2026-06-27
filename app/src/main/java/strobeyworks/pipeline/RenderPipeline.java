package strobeyworks.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import strobeyworks.SWMain;
import strobeyworks.logger.Logger;
import strobeyworks.pipeline.RenderNode.RenderInputState;
import strobeyworks.pipeline.RenderNode.RenderNodeState;
import strobeyworks.pipeline.configs.RenderInputConfig;
import strobeyworks.platform.JSONManager;
import strobeyworks.rendernodes.AgentNode;
import strobeyworks.rendernodes.MixNode;
import strobeyworks.rendernodes.PerlinNode;
import strobeyworks.ui.core.UIRenderer;
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
        n1.setColorHigh(new Vec3(1f, 1f, 1f));
        
        PerlinNode n4 = addNode(PerlinNode.class, d);
        n4.setUIAPosition(new Vec2(140, 200));
        n4.setColorHigh(new Vec3(0f, 1f, 0f));
        
        RenderNode n2 = addNode(AgentNode.class, d);
        n2.setUIAPosition(new Vec2(100, 100));

        setOutputtingNode(n2);
        
        RenderNode n3 = addNode(MixNode.class, d);
        n3.setUIAPosition(new Vec2(200, 300));
        
        // Connections
        addInputToNode(n3, new RenderInputConfig(n1));
        addInputToNode(n3, new RenderInputConfig(n2));
        addInputToNode(n3, new RenderInputConfig(n4));
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
    
    public boolean addInputToNode(RenderNode node, RenderInputConfig config) {
        if (config==null||node==null||config.node()==null) return false;
        RenderNode input = config.node();
        
        if (input==node) return false;
        node.addInput(config);
        
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
        
        for (RenderNode i : n.getDistinctInputNodes()) {
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

        for (RenderPipelineListener l : subscribers) l.pipelineSaved("test1.show");
    }
    
    public void loadPipelineFromDisk() {
        Logger.info("Loading pipeline from disk");
        RenderPipelineState state = JSONManager.loadPipelineState("test1.show");
        
        configureFromState(state);
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
        
        // Link inputs
        for (RenderNodeState nodeState : state.nodes()) {
            RenderNode destination = loadedNodes.get(nodeState.id());
            if (destination == null || nodeState.inputs() == null) continue;
            
            for (RenderInputState inputState : nodeState.inputs()) {
                RenderNode source = loadedNodes.get(inputState.nodeID());
                if (source == null) continue;

                addInputToNode(destination, new RenderInputConfig(source));
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
        
        for (RenderPipelineListener l : subscribers) l.pipelineLoaded();
    }
    
    public void iterate() {
        for (RenderNode n : compiledOrder) n.update();
        for (RenderNode n : compiledOrder) n.render();
    }
    
    public void cleanup() {
        for (RenderNode n : allNodes) n.cleanup();
    }
}
