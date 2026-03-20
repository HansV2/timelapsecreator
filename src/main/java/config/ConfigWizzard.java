package config;

import dto.Config;

import java.awt.*;
import java.util.List;
import java.util.*;

public class ConfigWizzard {

    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";

    private static final Map<String, Color> COLOR_OPTIONS = new LinkedHashMap<>();

    static {
        COLOR_OPTIONS.put("Black", Color.BLACK);
        COLOR_OPTIONS.put("White", Color.WHITE);
        COLOR_OPTIONS.put("Red", Color.RED);
        COLOR_OPTIONS.put("Green", Color.GREEN);
        COLOR_OPTIONS.put("Blue", Color.BLUE);
        COLOR_OPTIONS.put("Yellow", Color.YELLOW);
        COLOR_OPTIONS.put("Cyan", Color.CYAN);
        COLOR_OPTIONS.put("Magenta", Color.MAGENTA);
        COLOR_OPTIONS.put("Dark Gray", Color.DARK_GRAY);
        COLOR_OPTIONS.put("Light Gray", Color.LIGHT_GRAY);
    }

    private final ConfigService configService;
    private final Scanner scanner;

    public ConfigWizzard() {
        this.configService = new ConfigService();
        this.scanner = new Scanner(System.in);
    }

    public Config start() {
        Config config = configService.load();

        clearScreen();
        printHeader();

        System.out.println(CYAN + "  Use default / saved config?" + RESET);
        System.out.println("  Current values will be shown in " + YELLOW + "yellow" + RESET);
        System.out.print("  Use default config? (y/n): ");

        if (readBoolean()) {
            printDone();
            return config;
        }

        config = runWizard(config);
        configService.save(config);

        printDone();
        return config;
    }

    private Config runWizard(Config config) {
        String steps = "6";
        int currentStep = 1;
        config.setTimeLapseImagesPath(
                promptString("Step " + currentStep++ + "/" + steps + " | Input images folder path",
                        config.getTimeLapseImagesPath()));

        config.setOutputTimelapseVideoFolderPath(
                promptString("Step " + currentStep++ + "/" + steps + " | Output timelapse-video folder path",
                        config.getOutputTimelapseVideoFolderPath()));

        config.setCreateTimeLapseAndNumbersVideo(
                promptBoolean("Step " + currentStep++ + "/" + steps + " | Create both timelapse AND numbers video?",
                        config.getCreateTimeLapseAndNumbersVideo()));

        if (config.getCreateTimeLapseAndNumbersVideo()) {
            steps = "12";
            config.setOutputNumbersVideoFolderPath(
                    promptString("Step " + currentStep++ + "/" + steps + " | Output day-number-video folder path",
                            config.getOutputNumbersVideoFolderPath()));

            config.setNumberVideoSkipDaysWithoutImages(
                    promptBoolean("Step " + currentStep++ + "/" + steps + " | Skip days without images in numbers video?",
                            config.getNumberVideoSkipDaysWithoutImages()));

            config.setNumberVideoWidth(
                    promptInt("Step " + currentStep++ + "/" + steps + " | Numbers video width (px)",
                            config.getNumberVideoWidth(), 1, 7680));

            config.setNumberVideoHeight(
                    promptInt("Step " + currentStep++ + "/" + steps + " | Numbers video height (px)",
                            config.getNumberVideoHeight(), 1, 4320));

            config.setNumberVideoFontSize(
                    promptInt("Step " + currentStep++ + "/" + steps + " | Numbers video font size",
                            config.getNumberVideoFontSize(), 8, 1000));

            config.setNumberVideoBackgroundColor(
                    promptColor("Step " + currentStep++ + "/" + steps + " | Numbers video background color",
                            config.getNumberVideoBackgroundColor()));
            config.setNumberVideoFontColor(
                    promptColor("          | Numbers video font color",
                            config.getNumberVideoFontColor()));
        }

        config.setVideoFPS(
                promptInt("Step " + currentStep++ + "/" + steps + " | Video FPS (how many images should be shown per second of video)",
                        config.getVideoFPS(), 1, 120));

        config.setInputFileNamePattern(
                promptString("Step " + currentStep++ + "/" + steps + " | Input filename datetime pattern (e.g. yyyy-MM-dd_HH-mm-ss)",
                        config.getInputFileNamePattern()));

        config.setShowFfmpegLogOutput(
                promptBoolean("Step " + currentStep++ + "/" + steps + " | Show ffmpeg logs?",
                        config.isShowFfmpegLogOutput()));
        return config;
    }

    // -------------------------------------------------------------------------
    // Prompt helpers
    // -------------------------------------------------------------------------

    private String promptString(String label, String current) {
        clearScreen();
        printHeader();
        printStep(label, current);
        System.out.print("  New value (Enter to keep): ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? current : input;
    }

    private boolean promptBoolean(String label, boolean current) {
        clearScreen();
        printHeader();
        printStep(label, current ? "yes" : "no");
        System.out.print("  New value (y/n, Enter to keep): ");
        String input = scanner.nextLine().trim().toLowerCase();
        if (input.isEmpty()) return current;
        return readBooleanFrom(input);
    }

    private int promptInt(String label, int current, int min, int max) {
        while (true) {
            clearScreen();
            printHeader();
            printStep(label, String.valueOf(current));
            System.out.printf("  New value (%d-%d, Enter to keep): ", min, max);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return current;
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) return value;
                System.out.println(RED + "  Please enter a number between " + min + " and " + max + "." + RESET);
                System.out.print("  Press Enter to try again...");
                scanner.nextLine();
            } catch (NumberFormatException e) {
                System.out.println(RED + "  Invalid input — numbers only." + RESET);
                System.out.print("  Press Enter to try again...");
                scanner.nextLine();
            }
        }
    }

    private Color promptColor(String label, Color current) {
        while (true) {
            clearScreen();
            printHeader();
            printStep(label, colorName(current));
            System.out.println();

            List<String> names = new ArrayList<>(COLOR_OPTIONS.keySet());
            for (int i = 0; i < names.size(); i++) {
                System.out.printf("    [%2d] %s%n", i + 1, names.get(i));
            }
            System.out.println();

            System.out.printf("  Choose (1-%d, Enter to keep): ", names.size());
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return current;
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= names.size()) {
                    return COLOR_OPTIONS.get(names.get(choice - 1));
                }
                System.out.println(RED + "  Please enter a number between 1 and " + names.size() + "." + RESET);
                System.out.print("  Press Enter to try again...");
                scanner.nextLine();
            } catch (NumberFormatException e) {
                System.out.println(RED + "  Invalid input — numbers only." + RESET);
                System.out.print("  Press Enter to try again...");
                scanner.nextLine();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Input utilities
    // -------------------------------------------------------------------------

    private boolean readBoolean() {
        while (true) {
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) return true;
            if (input.equals("n") || input.equals("no")) return false;
            System.out.print(RED + "  Please enter y or n: " + RESET);
        }
    }

    private boolean readBooleanFrom(String input) {
        return input.equals("y") || input.equals("yes");
    }

    private String colorName(Color color) {
        return COLOR_OPTIONS.entrySet().stream()
                .filter(e -> e.getValue().equals(color))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
    }

    // -------------------------------------------------------------------------
    // Visual helpers
    // -------------------------------------------------------------------------

    private void printHeader() {
        System.out.println();
        System.out.println(BOLD + CYAN + "  ╔══════════════════════════════════════╗" + RESET);
        System.out.println(BOLD + CYAN + "  ║     TimeLapse Creator  —  Setup      ║" + RESET);
        System.out.println(BOLD + CYAN + "  ╚══════════════════════════════════════╝" + RESET);
        System.out.println();
    }

    private void printDivider() {
        System.out.println(CYAN + "  ────────────────────────────────────────" + RESET);
    }

    private void printStep(String label, String current) {
        System.out.println();
        printDivider();
        System.out.println("  " + BOLD + label + RESET);
        System.out.println("  Current: " + YELLOW + current + RESET);
    }

    private void printDone() {
        clearScreen();
        printHeader();
        System.out.println(GREEN + "  ✔  Config ready. Starting..." + RESET);
        System.out.println();
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}