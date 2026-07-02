package strobeyworks.rendernodes;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glDrawBuffer;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_COPY;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.GL_R32F;
import static org.lwjgl.opengl.GL30.GL_RASTERIZER_DISCARD;
import static org.lwjgl.opengl.GL30.GL_TRANSFORM_FEEDBACK_BUFFER;
import static org.lwjgl.opengl.GL30.glBeginTransformFeedback;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glEndTransformFeedback;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

import strobeyworks.SWMain;
import strobeyworks.pipeline.RenderNode;
import strobeyworks.pipeline.controls.ControlConfig.FloatControlConfig;
import strobeyworks.pipeline.controls.ControlItem.ControlGroup;
import strobeyworks.pipeline.controls.ControlItem.ControlTab;
import strobeyworks.pipeline.input.TextureInput;
import strobeyworks.platform.MidiManager.MidiEvent;
import strobeyworks.platform.MidiManager.MidiHandle;
import strobeyworks.platform.MidiSubscriber;
import strobeyworks.platform.ShaderManager;
import strobeyworks.utils.BindableList;
import strobeyworks.utils.BindableValue;
import strobeyworks.utils.Utils;
import strobeyworks.utils.Vec2;
import strobeyworks.utils.Vec2I;
import strobeyworks.utils.Vec3;

public class AgentNode extends RenderNode implements MidiSubscriber {
    
    private int agentPassProgram;
    private int depositPassProgram;
    private int diffusePassProgram;
    private int finalPassProgram;
    
    private int agentVBOWrite;
    private int agentVBORead;
    private int agentVAOWrite;
    private int agentVAORead;
    
    private int[] depositTextures = new int[2];
    private int[] depositFBOs = new int[2];
    private int depositReadIndex = 0;
    
    private int numAgents = 1000000;
    
    private BindableList<Vec3> stopColors;
    private BindableList<Float> stopPositions;
    
    private BindableValue<Boolean> togg;
    
    private HashMap<MidiHandle, Consumer<MidiEvent>> midiHandleMap;
    
    public AgentNode() {
        this(UUID.randomUUID());
    }
    
    public AgentNode(UUID id) {
        super(id, "Species-Agents", "agent", true);
        
        //loadDefaults();
        
        stopColors = new BindableList<>();
        stopPositions = new BindableList<>();
        
        stopColors.set(new Vec3(0.0f, 0.0f, 0.0f));
        stopColors.set(new Vec3(0.2f, 0.0f, 0.3f));
        stopColors.set(new Vec3(0.5f, 0.5f, 0.9f));
        
        stopPositions.set(0.0f);
        stopPositions.set(0.5f);
        stopPositions.set(1.0f);
        
        midiHandleMap = new HashMap<>();
        loadDefaultMidiMap();
    }

    @Override
    protected void setupFeeds() {}
    
    @Override
    protected void setupParameters() {
        ControlTab t = createControlTab("Agents");
        ControlGroup g = createControlGroup("Turning", t);
        
        floatParam(g, "Sensor Angle", "uSensorAngle", false, 0.02f, 3f, 3, 0.1f, 0.6f, true);
        floatParam(g, "Sensor Distance", "uSensorDistance", false, 0f, 0.5f, 2, 0.01f, 0.01f, true);
        floatParam(g, "Turn Speed", "uTurnSpeed", false, 0.2f, 20f, 2, 1f, 2f, true);
        floatParam(g, "Speed", "uSpeed", false, 0f, 0.5f, 2, 0.01f, 0.05f, true);
        
        g = createControlGroup("Opacity", t);
        
        floatParam(g, "Diffusion", "uDiffusion", false, 0f, 1.5f, 2, 0.01f, 0.1f, true);
        floatParam(g, "Decay", "uDecay", false, 0f, 10f, 2, 1f, 4f, true);
        floatParam(g, "Cuttoff", "uOpacityCuttoff", false, 0f, 1f, 2, 0.01f, 0f, true);
        floatParam(g, "Contribution", "uPheramoneContribution", false, 0f, 1f, 2, 0.01f, 1f, true);
        
        g = createControlGroup("Random", t);
        
        floatParam(g, "Random Turn", "uRandomTurnStrength", false, 0f, 20f, 2, 0.01f, 0f, true);
        floatParam(g, "Random Speed", "uRandomSpeedStrength", false, 0f, 2f, 2, 0.01f, 0f, true);
    }

    @Override
    public void feedInputsChanged() {}
    
    public void loadDefaultMidiMap() {
        /*MidiManager m = MidiManager.getInstance();
        
        midiHandleMap.put(m.getHandle(MidiHandleType.FADER, 1), e -> tempFloat(diffusion, e));
        midiHandleMap.put(m.getHandle(MidiHandleType.FADER, 2), e -> tempFloat(decay, e));
        midiHandleMap.put(m.getHandle(MidiHandleType.FADER, 3), e -> tempFloat(sensorAngle, e));
        midiHandleMap.put(m.getHandle(MidiHandleType.FADER, 4), e -> tempFloat(turnSpeed, e));
        midiHandleMap.put(m.getHandle(MidiHandleType.FADER, 5), e -> tempFloat(pheramoneContribution, e));
        
        midiHandleMap.put(m.getHandle(MidiHandleType.BUTTON, 11), e -> flashFloat(speed, e));
        
        m.subscribe(this, m.getHandle(MidiHandleType.FADER, 1));
        m.subscribe(this, m.getHandle(MidiHandleType.FADER, 2));
        m.subscribe(this, m.getHandle(MidiHandleType.FADER, 3));
        m.subscribe(this, m.getHandle(MidiHandleType.FADER, 4));
        m.subscribe(this, m.getHandle(MidiHandleType.FADER, 5));
        
        m.subscribe(this, m.getHandle(MidiHandleType.BUTTON, 11));*/
    }
    
    private void tempFloat(BindableValue<Float> b, MidiEvent e) {
        /*ControlConfig<Float> c = getFloatControlConfig(b);
        if (c==null) return;
        c.binding().setValue(Utils.lerpFloat(c.min(), c.max(), e.value()));*/
    }
    
    private void flashFloat(BindableValue<Float> b, MidiEvent e) {
        /*ControlConfig<Float> c = getFloatControlConfig(b);
        if (c==null) return;
        
        float v;
        if (e.noteType()==MidiType.NOTE_ON) v = c.max();
        else v = c.defaultValue();
        
        c.binding().setValue(v);*/
    }
    
    @Override
    public void receiveMidiEvent(MidiEvent event) {
        Consumer<MidiEvent> c = midiHandleMap.get(event.handle());
        if (c==null) return;
        c.accept(event);
    }
    
    public void randomize() {
        /*diffusion.setValue(Utils.randomBetween(0.1f, 1f));
        decay.setValue(Utils.randomBetween(2f, 8f));
        speed.setValue(Utils.randomBetween(0.001f, 0.1f));
        
        sensorDistance.setValue(Utils.randomBetween(0.01f, 0.15f));
        sensorAngle.setValue(Utils.randomBetween(0.01f, 2f));
        turnSpeed.setValue(Utils.randomBetween(1f, 15f));
        randomTurnStrength.setValue(Utils.randomBetween(0f, 4f));
        randomSpeedStrength.setValue(Utils.randomBetween(0f, 1f));
        
        pheramoneContribution.setValue(Utils.randomBetween(0.1f, 1f));*/
    }
    
    @Override
    public void initialise(Vec2I dimensions) {
        super.initialise(dimensions);
        ShaderManager sM = ShaderManager.getInstance();
        
        float[] agents = initialiseAgents();
        initialiseAgentBuffers(agents);
        initialiseDepositTextures();
        initialiseFullQuad();
        
        // Shaders init
        agentPassProgram = sM.createFeedbackProgram("agent/agent_pass.vert", "oAgent");
        depositPassProgram = sM.createProgram("agent/deposit_pass.vert", "agent/deposit_pass.frag");
        diffusePassProgram = sM.createProgram("agent/final_pass.vert", "agent/diffuse_pass.frag");
        finalPassProgram = sM.createProgram("agent/final_pass.vert", "agent/final_pass.frag");
        
        sM.bindVAO(0);
        sM.bindVBO(0);
        sM.useProgram(0);
    }
    
    private float[] initialiseAgents() {
        // Agent init
        float[] agents = new float[numAgents * 3];
        
        // Positioning
        halfScreenCirclePosition(agents);
        
        //Heading
        randomHeading(agents);
        
        return agents;
    }
    
    private void thinCirclePositon(float[] agents) {
        double angleStep = (Math.PI*2)/numAgents;
        
        for (int i=0; i<numAgents; i++) {
            double angle = i*angleStep;
            float r = Utils.randomBetween(0.195f, 0.2f);
            
            agents[i*3] = (float) (0.5+r*Math.cos(angle));
            agents[i*3+1] = (float) (0.5+r*Math.sin(angle));
        }
    }
    
    private void halfScreenCirclePosition(float[] agents) {
        double angleStep = (Math.PI*2)/numAgents;
        
        for (int i=0; i<numAgents; i++) {
            double angle = i*angleStep;
            float r = Utils.randomBetween(0.1f, 0.4f);
            
            agents[i*3] = (float) (0.5+r*Math.cos(angle));
            agents[i*3+1] = (float) (0.5+r*Math.sin(angle));
        }
    }
    
    private void halfScreenSquarePosition(float[] agents) {
        for (int i=0; i<numAgents; i++) {
            agents[i*3] = Utils.randomBetween(0.2f, 0.7f);
            agents[i*3+1] = Utils.randomBetween(0.2f, 0.7f);
        }
    }
    
    private void fullScreenPosition(float[] agents) {
        for (int i=0; i<numAgents; i++) {
            agents[i*3] = Utils.randomBetween(0f, 1f);
            agents[i*3+1] = Utils.randomBetween(0f, 1f);
        }
    }
    
    private void circleOutwardsHeading(float[] agents) {
        double angleStep = (Math.PI*2)/numAgents;
        
        for (int i=0; i<numAgents; i++) {
            double angle = i*angleStep;
            float h = (float) ((angle+Math.PI));
            h += (Math.random()*10);
            agents[i*3+2] = h;
        }
    }
    
    private void centerInwardsHeading(float[] agents) {
        for (int i = 0; i < numAgents; i++) {
            float x = agents[i * 3];
            float y = agents[i * 3 + 1];
            
            float dx = 0.5f - x;
            float dy = 0.5f - y;
            
            agents[i * 3 + 2] = (float) Math.atan2(dy, dx);
        }
    }
    
    private void randomHeading(float[] agents) {
        for (int i=0; i<numAgents; i++) {
            float h = Utils.randomBetween(0f, (float) (Math.PI * 2.0));
            agents[i*3+2] = h;
        }
    }
    
    
    private void initialiseAgentBuffers(float[] agents) {
        // Agent buffer 1
        agentVAORead = glGenVertexArrays();
        glBindVertexArray(agentVAORead);
        agentVBORead = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, agentVBORead);
        glBufferData(GL_ARRAY_BUFFER, agents, GL_DYNAMIC_COPY);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        // Agent buffer 2
        agentVAOWrite = glGenVertexArrays();
        glBindVertexArray(agentVAOWrite);
        agentVBOWrite = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, agentVBOWrite);
        glBufferData(GL_ARRAY_BUFFER, (long) numAgents * 3 * Float.BYTES, GL_DYNAMIC_COPY);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
    }
    
    private void initialiseDepositTextures() {        
        for (int i = 0; i < 2; i++) {
            depositTextures[i] = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, depositTextures[i]);
            
            glTexImage2D(
                GL_TEXTURE_2D, 0, GL_R32F,
                getOutputWidth(), getOutputHeight(), 0,
                GL_RED, GL_FLOAT, 0
            );
            
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            
            depositFBOs[i] = glGenFramebuffers();
            glBindFramebuffer(GL_FRAMEBUFFER, depositFBOs[i]);
            
            glFramebufferTexture2D(
                GL_FRAMEBUFFER,
                GL_COLOR_ATTACHMENT0,
                GL_TEXTURE_2D,
                depositTextures[i],
                0
            );
            
            glDrawBuffer(GL_COLOR_ATTACHMENT0);
            
            if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
                throw new RuntimeException("Deposit framebuffer is incomplete");
            }
            
            // Clear after attaching the texture.
            glClearColor(0f, 0f, 0f, 1f);
            glClear(GL_COLOR_BUFFER_BIT);
        }
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    
    @Override
    protected void handleOutputResize() {
        super.handleOutputResize();
        
        // Resize deposit textures
        for (int i = 0; i < 2; i++) {
            glBindTexture(GL_TEXTURE_2D, depositTextures[i]);
            
            glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_R32F,
                getOutputWidth(),
                getOutputHeight(),
                0,
                GL_RED,
                GL_FLOAT,
                0
            );
            
            glBindFramebuffer(GL_FRAMEBUFFER, depositFBOs[i]);
            
            if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
                throw new RuntimeException("Deposit framebuffer is incomplete after resize");
            }
            
            glClearColor(0f, 0f, 0f, 1f);
            glClear(GL_COLOR_BUFFER_BIT);
        }
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        
        depositReadIndex = 0;
    }
    
    @Override
    public void render() {
        ShaderManager sM = ShaderManager.getInstance();
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        agentUpdatePass(sM);
        diffusePass(sM);
        agentDepositPass(sM);
        finalRenderPass(sM);
    }
    
    private void agentUpdatePass(ShaderManager sM) {
        sM.useProgram(agentPassProgram);
        sM.setCurrentProgram(agentPassProgram);
        
        sM.setUniformFloat("uDeltaTime", SWMain.getDeltaTime());
        sM.setUniformFloat("uTime", (float) SWMain.getTotalTime());
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, depositTextures[depositReadIndex]);
        
        sM.setUniformInt("uDepositTexture", 0);
        
        uploadInputs(sM);
        
        glBindVertexArray(agentVAORead);
        
        glBindBufferBase(
            GL_TRANSFORM_FEEDBACK_BUFFER,
            0,
            agentVBOWrite
        );
        
        glEnable(GL_RASTERIZER_DISCARD);
        
        glBeginTransformFeedback(GL_POINTS);
        glDrawArrays(GL_POINTS, 0, numAgents);
        glEndTransformFeedback();
        glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, 0);
        
        glDisable(GL_RASTERIZER_DISCARD);
        
        // Swap buffers
        int temp = agentVBORead;
        agentVBORead = agentVBOWrite;
        agentVBOWrite = temp;
        
        temp = agentVAORead;
        agentVAORead = agentVAOWrite;
        agentVAOWrite = temp;
        
        // Reset
        glActiveTexture(GL_TEXTURE0);
        sM.bindVAO(0);
        sM.useProgram(0);
    }
    
    private void diffusePass(ShaderManager sM) {
        int writeIndex = 1 - depositReadIndex;
        
        glBindFramebuffer(GL_FRAMEBUFFER, depositFBOs[writeIndex]);
        glViewport(0, 0, getOutputWidth(), getOutputHeight());
        glDisable(GL_BLEND);
        
        sM.useProgram(diffusePassProgram);
        sM.setCurrentProgram(diffusePassProgram);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, depositTextures[depositReadIndex]);
        
        sM.setUniformInt("uDepositTexture", 0);
        sM.setUniformVec2("uTexelSize", new Vec2(1f/getOutputWidth(), 1f/getOutputHeight()));
        sM.setUniformFloat("uDeltaTime", SWMain.getDeltaTime());
        
        uploadInputs(sM);
        
        bindAndDrawFullScreen();
        
        depositReadIndex = writeIndex;
    }
    
    private void agentDepositPass(ShaderManager sM) {
        glBindFramebuffer(GL_FRAMEBUFFER, depositFBOs[depositReadIndex]);
        glViewport(
            0,
            0,
            getOutputWidth(),
            getOutputHeight()
        );
        
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);
        
        sM.useProgram(depositPassProgram);
        sM.setCurrentProgram(depositPassProgram);
        
        uploadInputs(sM);
        
        glBindVertexArray(agentVAORead);
        glDrawArrays(GL_POINTS, 0, numAgents);
        
        glBindVertexArray(0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    
    private void finalRenderPass(ShaderManager sM) {
        getRenderTarget().bind();
        
        glDisable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);
        
        sM.useProgram(finalPassProgram);
        sM.setCurrentProgram(finalPassProgram);
        
        uploadInputs(sM);
        
        int maxStops = 5;
        sM.setUniformInt("uStopCount", stopColors.size());
        
        float[] colors = new float[maxStops * 3];
        for (int i = 0; i < stopColors.size(); i++) {
            colors[i * 3 + 0] = stopColors.get(i).x;
            colors[i * 3 + 1] = stopColors.get(i).y;
            colors[i * 3 + 2] = stopColors.get(i).z;
        }
        
        float[] positions = new float[maxStops];
        for (int i = 0; i < stopPositions.size(); i++) {
            positions[i] = stopPositions.get(i); 
        }
        sM.setUniform3FV("uStopColors", colors);
        sM.setUniform1FV("uStopPositions", positions);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, depositTextures[depositReadIndex]);
        sM.setUniformInt("uDepositTexture", 0);
        
        bindAndDrawFullScreen();
        
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
        sM.useProgram(0);
    }
    
    @Override
    protected void handleCleanup() {
        // Cleanup deposit textures
        for (int i=0; i<2; i++) {
            if (depositFBOs[i]!=0) {
                glDeleteFramebuffers(depositFBOs[i]);
                depositFBOs[i] = 0;
            }
            if (depositTextures[i] != 0) {
                glDeleteTextures(depositTextures[i]);
                depositTextures[i] = 0;
            }
        }
        
        glDeleteVertexArrays(agentVAORead);
        glDeleteVertexArrays(agentVAOWrite);
        glDeleteBuffers(agentVBORead);
        glDeleteBuffers(agentVBOWrite);
    }
}
