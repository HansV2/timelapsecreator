package timelapse;

import dto.Config;
import dto.Day;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class TimeLapseProcessor {
    private  final DateTimeFormatter dateTimeFormatter;
    private final File folder;
    private final Config config;

    public TimeLapseProcessor(File folder, Config config) {
        this.config = config;
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(config.getInputFileNamePattern());

        if (!folder.isDirectory()) {
            throw new RuntimeException("Not a folder.");
        }
        this.folder = folder;
    }

    public List<Day> process() {
        File[] files = folder.listFiles();
        if (files == null) return new ArrayList<>();

        // TreeMap keeps date keys sorted; images per day sorted by filename
        TreeMap<LocalDate, List<File>> byDate = new TreeMap<>();

        for (File file : files) {
            if (!file.isFile()) continue;
            String name = file.getName();
            if (!name.endsWith(".jpg")) continue;
            String nameWithoutExtension = name.substring(0, name.lastIndexOf('.'));
            try {
                LocalDate date = LocalDateTime.parse(nameWithoutExtension, dateTimeFormatter).toLocalDate();
                byDate.computeIfAbsent(date, k -> new ArrayList<>()).add(file);
            } catch (DateTimeParseException e) {
                // filename doesn't match expected pattern, skip it
            }
        }

        if (byDate.isEmpty()) return new ArrayList<>();

        // Sort images within each day by filename
        for (List<File> images : byDate.values()) {
            images.sort(Comparator.comparing(File::getName));
        }

        // Iterate every calendar day from first to last, carrying forward the last
        // image of the previous day for any date that has no images of its own
        LocalDate first = byDate.firstKey();
        LocalDate last = byDate.lastKey();

        List<Day> days = new ArrayList<>();
        int dayNumber = 1;
        File lastImage = null;

        for (LocalDate date = first; !date.isAfter(last); date = date.plusDays(1)) {
            if (byDate.containsKey(date)) {
                List<File> images = byDate.get(date);
                lastImage = images.get(images.size() - 1);
                days.add(new Day(dayNumber, images));
            } else if(!this.config.getNumberVideoSkipDaysWithoutImages()){
                // No images this day — carry forward the last known image
                List<File> images = Collections.singletonList(lastImage);
                days.add(new Day(dayNumber, images));
            }
            ++dayNumber;
        }

        return days;
    }
}