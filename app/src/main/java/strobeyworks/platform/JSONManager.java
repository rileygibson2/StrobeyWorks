package strobeyworks.platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import strobeyworks.pipeline.RenderPipeline.RenderPipelineState;

public class JSONManager {
    private static final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    private static final Path SAVE_DIR = Paths.get("showfiles");

    public static void savePipelineState(String fileName, RenderPipelineState state) {
        try {
            Files.createDirectories(SAVE_DIR);
            Path path = SAVE_DIR.resolve(fileName.endsWith(".json") ? fileName : fileName + ".json");
            Files.writeString(path, gson.toJson(state));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save pipeline", e);
        }
    }

    public static RenderPipelineState loadPipelineState(String fileName) {
        try {
            Path path = SAVE_DIR.resolve(fileName.endsWith(".json") ? fileName : fileName + ".json");
            return gson.fromJson(Files.readString(path), RenderPipelineState.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load pipeline", e);
        }
    }
}
