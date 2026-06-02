package strobeyworks.noiserender;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
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
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
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
import static org.lwjgl.opengl.GL30.glEndTransformFeedback;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL11.glDrawBuffer;
import static org.lwjgl.opengl.GL11.GL_ONE;

import strobeyworks.SWMain;
import strobeyworks.platform.Animation;
import strobeyworks.platform.IOEvent;
import strobeyworks.platform.Renderer;
import strobeyworks.platform.ShaderManager;
import strobeyworks.utils.Utils;
import strobeyworks.utils.Vec2;

public class AgentRenderer extends Renderer {
    
    private static AgentRenderer instance;
    
    private int agentProgram;
    private int depositProgram;
    private int screenProgram;
    private int diffuseProgram;
    
    private int agentVBOWrite;
    private int agentVBORead;
    private int agentVAOWrite;
    private int agentVAORead;
    
    private int[] depositTextures = new int[2];
    private int[] depositFBOs = new int[2];
    private int depositReadIndex = 0;
    
    private int screenVAO;
    
    private int numAgents = 100000;
    private float diffusion = 0.1f;
    private float decay = 4f;
    private float speed = 0.1f;

    private float sensorDistance = 0.01f;
    private float sensorAngle = 0.6f;
    private float turnSpeed = 2;
    
    public static AgentRenderer getInstance() {
        if (instance==null) instance = new AgentRenderer();
        return instance;
    }
    
    private AgentRenderer() {
    }
    
    @Override
    public void receiveIOEvent(IOEvent event) {}
    
    @Override
    public void handleWindowResize() {}
    
    @Override
    public void addAnimation(Animation a) {}
    
    @Override
    public void removeAnimation(Animation a) {}
    
    @Override
    public void initialise() {
        ShaderManager sM = SWMain.getShaderManager();
        
        initialiseAgents();
        initialiseDepositTextures();
        initialiseScreenQuad();
        
        // Shaders init
        agentProgram = sM.createFeedbackProgram("agent/agent.vert", "oAgent");
        depositProgram = sM.createProgram("agent/deposit.vert", "agent/deposit.frag");
        diffuseProgram = sM.createProgram("agent/screen.vert", "agent/diffuse.frag");
        screenProgram = sM.createProgram("agent/screen.vert", "agent/screen.frag");
        
        sM.bindVAO(0);
        sM.bindVBO(0);
        sM.useProgram(0);
    }
    
    private void initialiseAgents() {
        // Agent init
        float[] agents = new float[numAgents * 3];
        
        double angleStep = (Math.PI*2)/numAgents;
        double radius = 0.1;
        
        for (int i = 0; i < numAgents; i++) {
            double angle = i*angleStep;
            float r = Utils.randomBetween(0.1f, 0.3f);


            float h = (float) ((angle+Math.PI));
            h += (Math.random()*10);

            agents[i * 3] = (float) (0.5+r*Math.cos(angle));
            agents[i * 3 + 1] = (float) (0.5+r*Math.sin(angle));
            agents[i * 3 + 2] = h;
        }
        
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
        int width = getParentWindow().getFramebufferWidth();
        int height = getParentWindow().getFramebufferHeight();
        
        for (int i = 0; i < 2; i++) {
            depositTextures[i] = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, depositTextures[i]);
            
            glTexImage2D(
                GL_TEXTURE_2D, 0, GL_R32F,
                width, height, 0,
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
    
    private void initialiseScreenQuad() {
        screenVAO = glGenVertexArrays();
        int screenVBO = glGenBuffers();
        
        glBindVertexArray(screenVAO);
        glBindBuffer(GL_ARRAY_BUFFER, screenVBO);
        
        glBufferData(GL_ARRAY_BUFFER, ShaderManager.QUAD_VERTICES, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    @Override
    public void update() {
        
    }
    
    @Override
    public void render() {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        agentUpdatePass();
        diffusePass();
        agentDepositPass();
        screenRenderPass();
    }
    
    private void agentUpdatePass() {
        ShaderManager sM = SWMain.getShaderManager();
        sM.useProgram(agentProgram);
        sM.setCurrentProgram(agentProgram);
        
        sM.setUniformFloat("uDeltaTime", SWMain.getDeltaTime());
        sM.setUniformFloat("uSpeed", speed);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, depositTextures[depositReadIndex]);
        
        sM.setUniformInt("uDepositTexture", 0);
        sM.setUniformFloat("uSensorDistance", sensorDistance);
        sM.setUniformFloat("uSensorAngle", sensorAngle);
        sM.setUniformFloat("uTurnSpeed", turnSpeed);
        
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
    
    private void diffusePass() {
        ShaderManager sM = SWMain.getShaderManager();
        
        int writeIndex = 1 - depositReadIndex;
        int width = getParentWindow().getFramebufferWidth();
        int height = getParentWindow().getFramebufferHeight();
        
        glBindFramebuffer(GL_FRAMEBUFFER, depositFBOs[writeIndex]);
        glViewport(0, 0, width, height);
        glDisable(GL_BLEND);
        
        sM.useProgram(diffuseProgram);
        sM.setCurrentProgram(diffuseProgram);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, depositTextures[depositReadIndex]);
        
        sM.setUniformInt("uDepositTexture", 0);
        sM.setUniformVec2("uTexelSize", new Vec2(1f/width, 1f/height));
        sM.setUniformFloat("uDeltaTime", SWMain.getDeltaTime());
        sM.setUniformFloat("uDiffusion", diffusion);
        sM.setUniformFloat("uDecay", decay);
        
        glBindVertexArray(screenVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        
        depositReadIndex = writeIndex;
    }
    
    private void agentDepositPass() {
        ShaderManager sM = SWMain.getShaderManager();
        
        glBindFramebuffer(GL_FRAMEBUFFER, depositFBOs[depositReadIndex]);
        glViewport(
            0,
            0,
            getParentWindow().getFramebufferWidth(),
            getParentWindow().getFramebufferHeight()
        );
        
        //glClearColor(0f, 0f, 0f, 1f);
        //glClear(GL_COLOR_BUFFER_BIT);
        
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);
        
        sM.useProgram(depositProgram);
        glBindVertexArray(agentVAORead);
        glDrawArrays(GL_POINTS, 0, numAgents);
        
        glBindVertexArray(0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    
    private void screenRenderPass() {
        ShaderManager sM = SWMain.getShaderManager();
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(
            0,
            0,
            getParentWindow().getFramebufferWidth(),
            getParentWindow().getFramebufferHeight()
        );
        
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);
        
        sM.useProgram(screenProgram);
        sM.setCurrentProgram(screenProgram);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, depositTextures[depositReadIndex]);
        sM.setUniformInt("uDepositTexture", 0);
        
        glBindVertexArray(screenVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
        sM.useProgram(0);
    }
}
