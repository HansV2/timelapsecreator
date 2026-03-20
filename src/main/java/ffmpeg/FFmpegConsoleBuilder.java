package ffmpeg;

import dto.Config;

import java.util.ArrayList;
import java.util.List;

public class FFmpegConsoleBuilder {

    private final List<String> parameters;
    private final Config config;

    public FFmpegConsoleBuilder(Config config) {
        this.config = config;
        this.parameters = new ArrayList<>();
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            parameters.add("cmd");
            parameters.add("/c");
        }
        parameters.add("ffmpeg");
        parameters.add("-y"); // always overwrite output files without asking
    }

    public FFmpegConsole build() {
        List<String> parametersCopy = new ArrayList<>(parameters);
        if (!config.isShowFfmpegLogOutput()) {
            parametersCopy.add("-loglevel");
            parametersCopy.add("quiet");
            parametersCopy.add("-nostats");
        }
        return new FFmpegConsole(parametersCopy, config);
    }

    public FFmpegConsoleBuilder addParameter(String parameter){
        parameters.add(parameter);
        return this;
    }
}
