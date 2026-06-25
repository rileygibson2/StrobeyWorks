package strobeyworks.pipeline;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import strobeyworks.SWMain;

public class RenderPipeline {
    
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

    public void subscribe(RenderPipelineListener listener) {
        subscribers.add(listener);
    }

    public void unsubscribe(RenderPipelineListener listener) {
        subscribers.remove(listener);
    }
    
    public <T extends RenderNode> T addNode(Class<T> nodeClass, int width, int height) {
        T node = null;
        try {
            node = nodeClass.getDeclaredConstructor().newInstance();
            allNodes.add(node);
            if (!compile()) throw new RuntimeException("Failed to create render node: compile failed");

            node.setupControls();
            RenderTarget target = RenderTarget.texture(width, height);
            node.setCustomName(node.getShortName()+(countNodesOfType(nodeClass)));
            node.setRenderTarget(target);
            node.initialise(width, height);
            
            return node;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create render node: " + nodeClass.getName(), e);
        }
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
    
    public boolean connectNodeInput(RenderNode inputNode, RenderNode destinationNode) {
        if (inputNode==null||destinationNode==null||inputNode==destinationNode) return false;
        destinationNode.addInputNode(inputNode);
        
        if (!compile()) {
            destinationNode.removeInputNode(inputNode);
            return false;
        }
        return true;
    }
    
    public boolean disconnectNodeInput(RenderNode inputNode, RenderNode destinationNode) {
        if (inputNode==null||destinationNode==null) return false;
        destinationNode.removeInputNode(inputNode);
        
        if (!compile()) {
            destinationNode.addInputNode(inputNode);
            return false;
        }
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
        
        for (RenderNode i : n.getInputNodes()) {
            if (!compileVisit(i, visiting, visited, order)) return false;
        }
        
        visiting.remove(n);
        visited.add(n);
        order.add(n);
        
        return true;
    }

    public void iterate() {
        for (RenderNode n : compiledOrder) n.update();
        for (RenderNode n : compiledOrder) n.render();
    }

    public void cleanup() {
        for (RenderNode n : allNodes) n.cleanup();
    }
}
