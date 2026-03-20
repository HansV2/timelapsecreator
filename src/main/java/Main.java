import config.ConfigWizzard;
import dto.Config;
import dto.Day;
import org.fusesource.jansi.AnsiConsole;
import timelapse.TimeLapseProcessor;
import timelapse.TimeLapseVideoGenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        AnsiConsole.systemInstall();
        new DependencyChecker().checkForFfmpeg();
        Config config = new ConfigWizzard().start();

        System.out.println("LET'S GOOOOOOOOOOO");

        boolean createTimeLapseAndNumbersVideo = config.getCreateTimeLapseAndNumbersVideo();
        String outputNumbersVideoFolderPath = config.getOutputNumbersVideoFolderPath();
        String outputTimelapseVideoFolderPath = config.getOutputTimelapseVideoFolderPath();
        String timeLapseImagesPath = config.getTimeLapseImagesPath();
        int videoFPS = config.getVideoFPS();

        File workingDir = new File(System.getProperty("user.dir"));
        File folder = resolvePath(workingDir, timeLapseImagesPath);

        TimeLapseProcessor timeLapseProcessor = new TimeLapseProcessor(folder, config);
        List<Day> process = timeLapseProcessor.process();

        System.out.println("Creating timelapse video...");
        File outputImages = resolvePath(workingDir, outputTimelapseVideoFolderPath + "imageLapse.mp4");
        TimeLapseVideoGenerator timeLapseVideoGenerator = new TimeLapseVideoGenerator(outputImages, config);
        timeLapseVideoGenerator.generateTimelapse(process, videoFPS);
        System.out.println("Finished with timelapse video: " + outputImages.getAbsolutePath());

        if(createTimeLapseAndNumbersVideo){
            System.out.println("Creating day-number video...");
            File outputNumbers = resolvePath(workingDir, outputNumbersVideoFolderPath + "daynumberLapse.mp4");
            timeLapseVideoGenerator.setOutputFile(outputNumbers);
            timeLapseVideoGenerator.generateDayCounter(process, videoFPS);
            System.out.println("Finished with day-number video: " + outputNumbers.getAbsolutePath());
        }

        System.out.println();
        System.out.print("I did everything I could! Press Enter to exit...");
        new Scanner(System.in).nextLine();
    }

    private static File resolvePath(File workingDir, String path) {
        File file = new File(path);
        return file.isAbsolute() ? file : new File(workingDir, path);
    }
}
