package dto;

import java.io.File;
import java.util.List;

public class Day {
    private int number;
    private List<File> images;

    public Day(int number, List<File> images) {
        this.number = number;
        this.images = images;
    }

    public int getNumber() {
        return number;
    }

    public List<File> getImages() {
        return images;
    }
}
