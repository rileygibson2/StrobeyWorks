package strobeyworks.platform;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import strobeyworks.pipeline.RenderTarget;
import strobeyworks.utils.Vec2;
import strobeyworks.utils.Vec2I;

public class OutputRenderer extends WindowRenderer {
    
    private int program;
    private int vao;
    private RenderTarget source;
    
    public void setSource(RenderTarget source) {
        this.source = source;
    }
    
    @Override public void handleWindowResize() {
        
    }
    
    @Override
    public void initialise() {
        ShaderManager sM = ShaderManager.getInstance();
        
        program = sM.createProgram("screen/screen_blit.vert", "screen/screen_blit.frag");
        
        vao = glGenVertexArrays();
        int vbo = glGenBuffers();
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, ShaderManager.QUAD_VERTICES, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    @Override
    public void render() {
        if (source == null) return;
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        Vec2I d = getParentWindow().getFramebufferDimensions();
        glViewport(0, 0, d.x, d.y);
        
        glDisable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);
        
        ShaderManager sM = ShaderManager.getInstance();
        sM.useProgram(program);
        sM.setCurrentProgram(program);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, source.getTexture());
        sM.setUniformInt("uTexture", 0);
        
        Vec2I sourceD = source.getDimensions();
        Vec2I windowD = getParentWindow().getFramebufferDimensions();
        float scaleX = (float) sourceD.x/windowD.x;
        float scaleY = (float) sourceD.y/windowD.y;
        sM.setUniformVec2("uScale", new Vec2(scaleX, scaleY));
        
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
        sM.useProgram(0);
    }
    
    @Override public void update() {}
    @Override public void addAnimation(Animation a) {}
    @Override public void removeAnimation(Animation a) {}
    @Override public void receiveIOEvent(IOEvent event) {}
}
