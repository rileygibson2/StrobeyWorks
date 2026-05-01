package strobeyworks;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import strobeyworks.utils.Vec3;
import strobeyworks.utils.Vec4;

public class ShaderManager {
    
    private int currentProgram;
    
    public int SCREEN_VAO;
    
    
    public ShaderManager() {
        clearCurrentProgram();
    }
    
    public void clearToBlack() {
        glClearColor(0f, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);
    }
    
    public void setCurrentProgram(int program) {
        clearCurrentProgram();
        currentProgram = program;
    }
    
    public void clearCurrentProgram() {
        currentProgram = -1;
    }
    
    public void useProgram(int prog) {
        glUseProgram(prog);
    }
    
    public void setBlend(boolean blend) {
        if (blend) glEnable(GL_BLEND);
        else glDisable(GL_BLEND);
    }
    
    public void bindFBO(int fBO) {glBindFramebuffer(GL_FRAMEBUFFER, fBO);}
    
    public void bindVBO(int vBO) {glBindBuffer(GL_ARRAY_BUFFER, vBO);}
    
    public void bindVAO(int vAO) {glBindVertexArray(vAO);}
    
    public void bindTexture(int tex) {glBindTexture(GL_TEXTURE_2D, tex);}
    
    public void assertViewport(int width, int height) {glViewport(0, 0, width, height);}
    
    public int createProgram(String vertPath, String fragPath) {
        String vertSrc = loadResource("/shaders/"+vertPath);
        String fragSrc = loadResource("/shaders/"+fragPath);
        int vs = compileShader(vertSrc, GL_VERTEX_SHADER);
        int fs = compileShader(fragSrc, GL_FRAGMENT_SHADER);
        
        int program = glCreateProgram();
        glAttachShader(program, vs);
        glAttachShader(program, fs);
        glLinkProgram(program);
        
        int status = glGetProgrami(program, GL_LINK_STATUS);
        if (status == GL_FALSE) {
            String log = glGetProgramInfoLog(program);
            throw new RuntimeException("Program link error: " + log);
        }
        
        glDeleteShader(vs);
        glDeleteShader(fs);
        
        return program;
    }
    
    public int getUniformLocation(String n) {
        if (currentProgram==-1) throw new RuntimeException("Cannot get uniform - no current program");
        return glGetUniformLocation(currentProgram, n);
    }
    
    public void setUniformVec3(String n, Vec3 v) {
        if (currentProgram==-1) throw new RuntimeException("Cannot set uniform - no current program");
        int id = glGetUniformLocation(currentProgram, n);
        glUniform3f(id, v.x, v.y, v.z);
    }


    public void setUniformVec4(String n, Vec4 v) {
        if (currentProgram==-1) throw new RuntimeException("Cannot set uniform - no current program");
        int id = glGetUniformLocation(currentProgram, n);
        glUniform4f(id, v.r, v.g, v.b, v.a);
    }
    
    public void setUniformFloat(String n, float v) {
        if (currentProgram==-1) throw new RuntimeException("Cannot set uniform - no current program");
        int id = glGetUniformLocation(currentProgram, n);
        glUniform1f(id, v);
    }
    
    public void setUniformInt(String n, int v) {
        if (currentProgram==-1) throw new RuntimeException("Cannot set uniform - no current program");
        int id = glGetUniformLocation(currentProgram, n);
        glUniform1i(id, v);
    }
    
    public void setUniformMat4(String n, Matrix4f m) {
        if (currentProgram == -1) throw new RuntimeException("Cannot set uniform - no current program");
        
        int id = glGetUniformLocation(currentProgram, n);
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            m.get(fb);
            glUniformMatrix4fv(id, false, fb);
        }
    }
    
    private int compileShader(String src, int type) {
        int id = glCreateShader(type);
        glShaderSource(id, src);
        glCompileShader(id);
        
        int status = glGetShaderi(id, GL_COMPILE_STATUS);
        if (status == GL_FALSE) {
            String log = glGetShaderInfoLog(id);
            throw new RuntimeException("Shader compile error: " + log);
        }
        return id;
    }
    
    private String loadResource(String path) {
        try (InputStream in = getClass().getResourceAsStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource: " + path, e);
        }
    }
}
