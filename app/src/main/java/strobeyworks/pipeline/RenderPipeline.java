package strobeyworks.pipeline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import strobeyworks.SWMain;
import strobeyworks.pipeline.configs.RenderInputConfig;
import strobeyworks.platform.Window;
import strobeyworks.rendernodes.AgentNode;
import strobeyworks.rendernodes.MixNode;
import strobeyworks.rendernodes.PerlinNode;
import strobeyworks.ui.core.UIRenderer;
import strobeyworks.utils.Vec2;
import strobeyworks.utils.Vec3;

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

    public void simulate() {
        // Nodes
        Window output = SWMain.getOutputWindow();
        PerlinNode n1 = addNode(PerlinNode.class, output.getFramebufferWidth(), output.getFramebufferHeight());
        n1.setUIAPosition(new Vec2(0, 0));
        n1.setColorHigh(new Vec3(1f, 1f, 1f));

        PerlinNode n4 = addNode(PerlinNode.class, output.getFramebufferWidth(), output.getFramebufferHeight());
        n4.setUIAPosition(new Vec2(140, 200));
        n4.setColorHigh(new Vec3(0f, 1f, 0f));
        
        RenderNode n2 = addNode(AgentNode.class, output.getFramebufferWidth(), output.getFramebufferHeight());
        n2.setUIAPosition(new Vec2(100, 100));

        UIRenderer.getInstance().setSelectedNode(n2);
        setOutputtingNode(n2);

        RenderNode n3 = addNode(MixNode.class, output.getFramebufferWidth(), output.getFramebufferHeight());
        n3.setUIAPosition(new Vec2(200, 300));

        // Connections
        addRenderInput(n3, new RenderInputConfig(n1));
        addRenderInput(n3, new RenderInputConfig(n2));
        addRenderInput(n3, new RenderInputConfig(n4));
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

    public void handleNodeControlsChanged() {
        for (RenderPipelineListener l : subscribers) l.nodeControlsChanged();
    }
    
    public boolean addRenderInput(RenderNode destinationNode, RenderInputConfig config) {
        if (config==null||destinationNode==null||config.node()==null) return false;
        RenderNode input = config.node();

        if (input==destinationNode) return false;
        destinationNode.addRenderInput(config);
        
        if (!compile()) {
            destinationNode.removeRenderInput(config);
            return false;
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

    public void iterate() {
        for (RenderNode n : compiledOrder) n.update();
        for (RenderNode n : compiledOrder) n.render();
    }

    public void cleanup() {
        for (RenderNode n : allNodes) n.cleanup();
    }
}
