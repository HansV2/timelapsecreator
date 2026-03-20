package config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dto.Config;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

public class ConfigService {

    private static final File CONFIG_DIR  = new File(System.getProperty("user.home"), ".timelapsecreator");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "config.json");

    private final ObjectMapper mapper;

    public ConfigService() {
        SimpleModule colorModule = new SimpleModule();
        colorModule.addSerializer(Color.class, new ColorSerializer());
        colorModule.addDeserializer(Color.class, new ColorDeserializer());

        mapper = new ObjectMapper();
        mapper.registerModule(colorModule);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public Config load() {
        Config defaults = defaults();
        if (!CONFIG_FILE.exists()) {
            return defaults;
        }
        try {
            return mapper.readValue(CONFIG_FILE, Config.class);
        } catch (IOException e) {
            System.err.println("Failed to load config, using defaults:\r\n" + defaults);
            return defaults;
        }
    }

    public void save(Config config) {
        try {
            CONFIG_DIR.mkdirs();
            mapper.writeValue(CONFIG_FILE, config);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config to " + CONFIG_FILE.getAbsolutePath(), e);
        }
    }

    private Config defaults() {
        Config config = new Config();
        config.setCreateTimeLapseAndNumbersVideo(true);
        config.setTimeLapseImagesPath("raw");
        config.setOutputNumbersVideoFolderPath("");
        config.setOutputTimelapseVideoFolderPath("");
        config.setVideoFPS(15);
        config.setNumberVideoWidth(1920);
        config.setNumberVideoHeight(1080);
        config.setNumberVideoFontSize(400);
        config.setNumberVideoBackgroundColor(Color.BLACK);
        config.setNumberVideoFontColor(Color.WHITE);
        config.setInputFileNamePattern("yyyy-MM-dd_HH-mm-ss");
        config.setNumberVideoSkipDaysWithoutImages(true);
        config.setShowFfmpegLogOutput(false);
        return config;
    }

    // Serializes Color as a hex string, e.g. "#FF0000"
    private static class ColorSerializer extends JsonSerializer<Color> {
        @Override
        public void serialize(Color color, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
        }
    }

    // Deserializes "#FF0000" back to a Color
    private static class ColorDeserializer extends JsonDeserializer<Color> {
        @Override
        public Color deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            return Color.decode(p.getText());
        }
    }
}