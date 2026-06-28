package strobeyworks.ui.core;

public class UITexture {
    
    private int textureID;
    private int width;
    private int height;

    private String fileName;
    
    public UITexture(String fileName, int textureID, int width, int height) {
        this.fileName = fileName;
        this.textureID = textureID;
        this.width = width;
        this.height = height;
    }
    
    public int getTextureId() {
        return this.textureID;
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public int getHeight() {
        return this.height;
    }

    public String getFileName() {
        return this.fileName;
    }
}
