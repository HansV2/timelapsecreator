package ffmpeg;

import dto.Config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FFmpegConsole {
    private static final String PATH_ENCLOSURE = System.getProperty("os.name").toLowerCase().contains("win") ? "\"" : "";

    private final List<String> parameters;
    private final ProcessBuilder processBuilder;

    public FFmpegConsole(List<String> parameters, Config config) {
        this.parameters = new ArrayList<>(parameters);
        ProcessBuilder pb = new ProcessBuilder(parameters);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT); // stdout
        if (!config.isShowFfmpegLogOutput()) {
            pb.redirectError(ProcessBuilder.Redirect.DISCARD); // suppress stderr entirely
        } else {
            pb.redirectError(ProcessBuilder.Redirect.INHERIT); // show stderr normally
        }
        processBuilder = pb;
    }

    public void run(File outputFile) throws IOException, InterruptedException {
        parameters.add(PATH_ENCLOSURE + outputFile.getAbsolutePath() + PATH_ENCLOSURE);
        run();
    }

    public void run() throws IOException, InterruptedException {
        processBuilder.command(parameters);
        Process start = processBuilder.start();
        if (start.waitFor() != 0) {
            throw new RuntimeException("Something went wrong while rotating video.");
        }
    }
}
