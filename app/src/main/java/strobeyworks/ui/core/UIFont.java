package strobeyworks.ui.core;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap;
import static org.lwjgl.stb.STBTruetype.stbtt_GetBakedQuad;
import static org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;
import static org.lwjgl.stb.STBTruetype.stbtt_ScaleForPixelHeight;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import strobeyworks.logger.Logger;

public class UIFont {
    
    private int textureId;
    private STBTTBakedChar.Buffer glyphData;
    
    private int atlasWidth;
    private int atlasHeight;
    private int firstChar;
    private int charCount;
    private float fontSize;
    
    private STBTTFontinfo fontInfo;
    private ByteBuffer fontBuffer;
    
    private float ascent;
    private float descent;
    private float lineGap;
    private float lineHeight;
    
    public void loadFromTTF(String fontName, float fontSize) {
        this.fontSize = fontSize;
        
        this.atlasWidth = 512;
        this.atlasHeight = 512;
        this.firstChar = 32;
        this.charCount = 96;
        
        try {
            fontBuffer = loadFontResourceToByteBuffer(fontName);
            
            fontInfo = STBTTFontinfo.create();
            
            if (!stbtt_InitFont(fontInfo, fontBuffer)) {
                throw new RuntimeException("Failed to init font: " + fontName);
            }
            
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer ascentBuf = stack.mallocInt(1);
                IntBuffer descentBuf = stack.mallocInt(1);
                IntBuffer lineGapBuf = stack.mallocInt(1);
                
                stbtt_GetFontVMetrics(fontInfo, ascentBuf, descentBuf, lineGapBuf);
                
                float scale = stbtt_ScaleForPixelHeight(fontInfo, fontSize);
                
                ascent = ascentBuf.get(0) * scale;
                descent = descentBuf.get(0) * scale;
                lineGap = lineGapBuf.get(0) * scale;
                
                lineHeight = ascent - descent + lineGap;
            }
            
            ByteBuffer atlasBitmap = BufferUtils.createByteBuffer(atlasWidth * atlasHeight);
            
            glyphData = STBTTBakedChar.malloc(charCount);
            
            int result = stbtt_BakeFontBitmap(
                fontBuffer,
                fontSize,
                atlasBitmap,
                atlasWidth,
                atlasHeight,
                firstChar,
                glyphData
            );
            
            if (result <= 0) {
                throw new RuntimeException("Failed to bake font atlas. Try a larger atlas.");
            }
            
            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);
            
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            
            glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RED,
                atlasWidth,
                atlasHeight,
                0,
                GL_RED,
                GL_UNSIGNED_BYTE,
                atlasBitmap
            );
            
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            
            glBindTexture(GL_TEXTURE_2D, 0);
            
        } catch (IOException e) {
            Logger.throwRuntimeException("Could not load font: " + fontName, e);
        }
    }
    
    
    public float[] buildTextVertices(String text, float x, float y) {
        ArrayList<Float> vertices = new ArrayList<>();
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer cursorX = stack.floats(x);
            FloatBuffer cursorY = stack.floats(y);
            
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                
                if (c == '\n') {
                    cursorX.put(0, x);
                    cursorY.put(0, cursorY.get(0) + fontSize);
                    continue;
                }
                
                if (c < firstChar || c >= firstChar + charCount) {
                    continue;
                }
                
                int charIndex = c - firstChar;
                
                stbtt_GetBakedQuad(
                    glyphData,
                    atlasWidth,
                    atlasHeight,
                    charIndex,
                    cursorX,
                    cursorY,
                    quad,
                    true
                );
                
                addQuad(vertices, quad);
            }
        }
        
        float[] result = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            result[i] = vertices.get(i);
        }
        
        return result;
    }
    
    private void addQuad(ArrayList<Float> vertices, STBTTAlignedQuad q) {
        float x0 = q.x0();
        float y0 = q.y0();
        float x1 = q.x1();
        float y1 = q.y1();
        
        float u0 = q.s0();
        float v0 = q.t0();
        float u1 = q.s1();
        float v1 = q.t1();
        
        addVertex(vertices, x0, y0, u0, v0);
        addVertex(vertices, x1, y0, u1, v0);
        addVertex(vertices, x1, y1, u1, v1);
        
        addVertex(vertices, x1, y1, u1, v1);
        addVertex(vertices, x0, y1, u0, v1);
        addVertex(vertices, x0, y0, u0, v0);
    }
    
    private void addVertex(ArrayList<Float> vertices, float x, float y, float u, float v) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(u);
        vertices.add(v);
    }
    
    public float measureTextWidth(String text) {
        float width = 0f;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < firstChar || c >= firstChar + charCount) continue;
            
            STBTTBakedChar glyph = glyphData.get(c - firstChar);
            width += glyph.xadvance();
        }
        
        return width;
    }

    public int getCursorIndexAt(String text, float x) {
        if (x<=0f) return 0;

        float cursorX = 0f;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < firstChar || c >= firstChar + charCount) continue;
            
            STBTTBakedChar glyph = glyphData.get(c - firstChar);
            float advance = glyph.xadvance();

            if (x<cursorX+glyph.xadvance()*0.5f) return i;
            cursorX += advance;
        }
        
        return text.length();
    }
    
    public float measureTextHeight(String text) {
        int lineCount = 1;
        
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lineCount++;
            }
        }
        
        return lineCount * lineHeight;
    }
    
    
    
    private ByteBuffer loadFontResourceToByteBuffer(String fontName) throws IOException {
        String resourcePath = "/fonts/" + fontName;
        
        try (InputStream in = UIFont.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Font resource not found: " + resourcePath);
            }
            
            byte[] bytes = in.readAllBytes();
            
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            
            return buffer;
        }
    }
    
    
    public int getTextureId() {
        return textureId;
    }
    
    public STBTTBakedChar.Buffer getGlyphData() {
        return glyphData;
    }
    
    public int getFirstChar() {
        return firstChar;
    }
    
    public float getFontSize() {
        return this.fontSize;
    }
    
    public float getAscent() {
        return ascent;
    }
    
    public float getDescent() {
        return descent;
    }
    
    public float getLineGap() {
        return lineGap;
    }
    
    public float getLineHeight() {
        return lineHeight;
    }
    
}
