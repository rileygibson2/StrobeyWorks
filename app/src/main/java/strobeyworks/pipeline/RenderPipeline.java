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
import strobeyworks.pipeline.input.RenderInput.RenderInputState;
import strobeyworks.pipeline.input.RenderInputSlot.RenderInputSlotState;
import strobeyworks.pipeline.input.TextureInput;
import strobeyworks.pipeline.input.TextureInput.TextureInputMode;
import strobeyworks.pipeline.input.TextureInput.TextureInputState;
import strobeyworks.platform.JSONManager;
import strobeyworks.rendernodes.MaskNode;
import strobeyworks.rendernodes.PerlinNode;
import strobeyworks.rendernodes.PixelDots;
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
        n1.setUIAPosition(-10, 0);
        //n1.setColorHigh(new Vec3(0f, 0f, 1f));
        
        PerlinNode n2 = addNode(PerlinNode.class, d);
        n2.setUIAPosition(-10, 120);
        n2.setColorHigh(new Vec3(1f, 0f, 0f));
        
        RenderNode n3 = addNode(MaskNode.class, d);
        n3.setUIAPosition(165, 60);
        
        RenderNode n4 = addNode(PixelDots.class, d);
        n4.setUIAPosition(165, 200);
        
        // Feed inputs
        n3.setFeedInput(0, new TextureInput(n1));
        n3.setFeedInput(1, new TextureInput(n2, TextureInputMode.RED));
        
        n4.setFeedInput(0, new TextureInput(n3));
        
        setOutputtingNode(n4);
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
        if (!tryCompile()) throw new RuntimeException("Failed to add render node: compile failed");
        
        node.setup();
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
    
    public void handleNodeInputsChanged(RenderNode node) {
        for (RenderPipelineListener l : subscribers) l.nodeInputsChanged(node);
    }
    
    public boolean disconnectNodeInput(RenderNode inputNode, RenderNode destinationNode) {
        return true;
    }
    
    boolean tryCompile() {
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
        
        for (RenderNode i : n.getDependancies()) {
            if (!compileVisit(i, visiting, visited, order)) return false;
        }
        
        visiting.remove(n);
        visited.add(n);
        order.add(n);
        
        return true;
    }
    
    private RenderPipelineState getState() {
        List<RenderNodeState> nodeStates = new ArrayList<>();
        for (RenderNode n : allNodes) nodeStates.add(n.getState());
        
        return new RenderPipelineState(
            nodeStates,
            outputtingNode.getIDString()
        );
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
            node.setup();
            RenderTarget target = RenderTarget.texture(dimensions);
            node.setRenderTarget(target);
            node.initialise(dimensions);
            
            loadedNodes.put(nodeState.id(), node);
        }
        
        // Set saved inputs
        for (RenderNodeState nodeState : state.nodes()) {
            RenderNode destination = loadedNodes.get(nodeState.id());
            if (destination==null) continue;
            
            for (RenderInputSlotState feedSlotState : nodeState.feedSlots()) {
                RenderInputState inputState = feedSlotState.input();
                if (inputState==null) continue;

                if (inputState.type().equals("texture")) {
                    TextureInputState texInputState = JSONManager.gson.fromJson(inputState.value(), TextureInputState.class);
                    
                    RenderNode source = loadedNodes.get(texInputState.nodeID());
                    if (source==null) continue;

                    TextureInputMode mode = TextureInputMode.UNNECESSARY;
                    if (texInputState.mode()!=null) mode = TextureInputMode.valueOf(texInputState.mode());

                    
                    
                }


                RenderNode source = loadedNodes.get(feedSlotState.nodeID());
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
        if (!tryCompile()) {
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
