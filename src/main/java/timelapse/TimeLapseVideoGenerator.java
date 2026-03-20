package timelapse;

import dto.Config;
import dto.Day;
import ffmpeg.FFmpegConsoleBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class TimeLapseVideoGenerator {

    private File outputFile;
    private final Config config;

    public TimeLapseVideoGenerator(File outputFile, Config config) {
        this.outputFile = outputFile;
        this.config = config;
    }

    public void generateTimelapse(List<Day> days, int fps) throws IOException, InterruptedException {
        File concatFile = File.createTempFile("timelapse_concat", ".txt");
        concatFile.deleteOnExit();

        double frameDuration = 1.0 / fps;
        StringBuilder sb = new StringBuilder();
        for (Day day : days) {
            for (File image : day.getImages()) {
                String path = image.getAbsolutePath().replace("'", "\\'");
                sb.append("file '").append(path).append("'\n");
                sb.append("duration ").append(frameDuration).append("\n");
            }
        }
        Files.writeString(concatFile.toPath(), sb.toString());

        new FFmpegConsoleBuilder(this.config)
                .addParameter("-f")
                .addParameter("concat")
                .addParameter("-safe")
                .addParameter("0")
                .addParameter("-i")
                .addParameter(concatFile.getAbsolutePath())
                .addParameter("-r")
                .addParameter(String.valueOf(fps))
                .addParameter("-c:v")
                .addParameter("libx264")
                .addParameter("-pix_fmt")
                .addParameter("yuv420p")
                .build()
                .run(outputFile);

        if (!isValidFile(outputFile)) {
            throw new RuntimeException("Process finished but no or empty output file");
        }
    }

    public void generateDayCounter(List<Day> days, int fps) throws IOException, InterruptedException {
        // Generate one black frame image per day with the day number drawn on it
        List<File> frameImages = new ArrayList<>();
        for (Day day : days) {
            File frameFile = File.createTempFile("day_counter_" + day.getNumber(), ".png");
            frameFile.deleteOnExit();
            renderDayFrame(day.getNumber(), frameFile);
            frameImages.add(frameFile);
        }

        // Build concat file — each day image shown for exactly as many frames
        // as that day has images in the timelapse
        File concatFile = File.createTempFile("daycounter_concat", ".txt");
        concatFile.deleteOnExit();

        double frameDuration = 1.0 / fps;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < days.size(); i++) {
            int frameCount = days.get(i).getImages().size();
            String path = frameImages.get(i).getAbsolutePath().replace("'", "\\'");
            for (int f = 0; f < frameCount; f++) {
                sb.append("file '").append(path).append("'\n");
                sb.append("duration ").append(frameDuration).append("\n");
            }
        }
        Files.writeString(concatFile.toPath(), sb.toString());

        new FFmpegConsoleBuilder(this.config)
                .addParameter("-f")
                .addParameter("concat")
                .addParameter("-safe")
                .addParameter("0")
                .addParameter("-i")
                .addParameter(concatFile.getAbsolutePath())
                .addParameter("-r")
                .addParameter(String.valueOf(fps))
                .addParameter("-c:v")
                .addParameter("libx264")
                .addParameter("-pix_fmt")
                .addParameter("yuv420p")
                .build()
                .run(outputFile);

        if (!isValidFile(outputFile)) {
            throw new RuntimeException("Process finished but no or empty output file");
        }
    }

    /**
     * Renders a 1920x1080 black PNG with the day number in white,
     * right-aligned with 50px padding, font size 200.
     */
    private void renderDayFrame(int dayNumber, File output) throws IOException {
        int width = this.config.getNumberVideoWidth();
        int height = this.config.getNumberVideoHeight();
        int fontSize = this.config.getNumberVideoFontSize();

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Color backgroundColor = this.config.getNumberVideoBackgroundColor();
        g.setColor(backgroundColor);
        g.fillRect(0, 0, width, height);

        Color fontColor = this.config.getNumberVideoFontColor();
        g.setColor(fontColor);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSize));

        String text = String.valueOf(dayNumber);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = width - textWidth - 50;
        int y = (height + fm.getAscent() - fm.getDescent()) / 2;

        g.drawString(text, x, y);
        g.dispose();

        ImageIO.write(img, "png", output);
    }

    private boolean isValidFile(File resultingFile) throws IOException {
        return resultingFile.exists() && Files.size(resultingFile.toPath()) > 0;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
}