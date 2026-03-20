package dto;

import java.awt.*;

public class Config {
    private boolean createTimeLapseAndNumbersVideo;
    private String timeLapseImagesPath;
    private String outputNumbersVideoFolderPath;
    private String outputTimelapseVideoFolderPath;
    private int videoFPS;
    private int numberVideoWidth;
    private int numberVideoHeight;
    private int numberVideoFontSize;
    private Color numberVideoBackgroundColor;
    private Color numberVideoFontColor;
    private String inputFileNamePattern;
    private boolean numberVideoSkipDaysWithoutImages;
    private boolean showFfmpegLogOutput;

    public boolean getCreateTimeLapseAndNumbersVideo() {
        return createTimeLapseAndNumbersVideo;
    }

    public void setCreateTimeLapseAndNumbersVideo(boolean createTimeLapseAndNumbersVideo) {
        this.createTimeLapseAndNumbersVideo = createTimeLapseAndNumbersVideo;
    }

    public String getTimeLapseImagesPath() {
        return timeLapseImagesPath;
    }

    public void setTimeLapseImagesPath(String timeLapseImagesPath) {
        this.timeLapseImagesPath = timeLapseImagesPath;
    }

    public String getOutputNumbersVideoFolderPath() {
        return outputNumbersVideoFolderPath;
    }

    public void setOutputNumbersVideoFolderPath(String outputNumbersVideoFolderPath) {
        this.outputNumbersVideoFolderPath = outputNumbersVideoFolderPath;
    }

    public int getVideoFPS() {
        return videoFPS;
    }

    public void setVideoFPS(int videoFPS) {
        this.videoFPS = videoFPS;
    }

    public int getNumberVideoWidth() {
        return numberVideoWidth;
    }

    public void setNumberVideoWidth(int numberVideoWidth) {
        this.numberVideoWidth = numberVideoWidth;
    }

    public int getNumberVideoHeight() {
        return numberVideoHeight;
    }

    public void setNumberVideoHeight(int numberVideoHeight) {
        this.numberVideoHeight = numberVideoHeight;
    }

    public int getNumberVideoFontSize() {
        return numberVideoFontSize;
    }

    public void setNumberVideoFontSize(int numberVideoFontSize) {
        this.numberVideoFontSize = numberVideoFontSize;
    }

    public Color getNumberVideoBackgroundColor() {
        return numberVideoBackgroundColor;
    }

    public void setNumberVideoBackgroundColor(Color numberVideoBackgroundColor) {
        this.numberVideoBackgroundColor = numberVideoBackgroundColor;
    }

    public Color getNumberVideoFontColor() {
        return numberVideoFontColor;
    }

    public void setNumberVideoFontColor(Color numberVideoFontColor) {
        this.numberVideoFontColor = numberVideoFontColor;
    }

    public String getInputFileNamePattern() {
        return inputFileNamePattern;
    }

    public void setInputFileNamePattern(String inputFileNamePattern) {
        this.inputFileNamePattern = inputFileNamePattern;
    }

    public boolean getNumberVideoSkipDaysWithoutImages() {
        return numberVideoSkipDaysWithoutImages;
    }

    public void setNumberVideoSkipDaysWithoutImages(boolean numberVideoSkipDaysWithoutImages) {
        this.numberVideoSkipDaysWithoutImages = numberVideoSkipDaysWithoutImages;
    }

    public String getOutputTimelapseVideoFolderPath() {
        return outputTimelapseVideoFolderPath;
    }

    public void setOutputTimelapseVideoFolderPath(String outputTimelapseVideoFolderPath) {
        this.outputTimelapseVideoFolderPath = outputTimelapseVideoFolderPath;
    }

    public boolean isShowFfmpegLogOutput() {
        return showFfmpegLogOutput;
    }

    public void setShowFfmpegLogOutput(boolean showFfmpegLogOutput) {
        this.showFfmpegLogOutput = showFfmpegLogOutput;
    }
}
