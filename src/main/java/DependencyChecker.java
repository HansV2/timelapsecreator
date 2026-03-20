import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

public class DependencyChecker {

    // Static builds — no installer needed on any platform
    private static final String DOWNLOAD_URL_WINDOWS =
            "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip";
    private static final String DOWNLOAD_URL_LINUX =
            "https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz";
    private static final String DOWNLOAD_URL_MAC =
            "https://evermeet.cx/ffmpeg/getrelease/zip";

    private static final String INSTALL_DIR =
            System.getProperty("user.home") + File.separator + ".timelapsecreator" + File.separator + "ffmpeg";

    private final Scanner scanner = new Scanner(System.in);

    public void checkForFfmpeg() {
        System.out.println("Checking for FFmpeg...");

        if (isFfmpegAvailable()) {
            System.out.println("FFmpeg found.");
            return;
        }

        System.out.println("FFmpeg was not found on this system.");
        System.out.println();
        System.out.print("Would you like to download and install FFmpeg automatically? (y/n): ");
        String input = scanner.nextLine().trim().toLowerCase();

        if (!input.equals("y") && !input.equals("yes")) {
            System.out.println("FFmpeg is required to run this application.");
            System.out.println("Download it manually from: https://ffmpeg.org/download.html");
            throw new RuntimeException("FFmpeg not found. Aborting.");
        }

        try {
            if (isWindows()) {
                downloadAndInstallWindows();
            } else if (isMac()) {
                downloadAndInstallMac();
            } else {
                downloadAndInstallLinux();
            }

            addToPath();

            System.out.println();
            System.out.println("FFmpeg installed to: " + INSTALL_DIR);
            System.out.println("NOTE: Restart your terminal for PATH changes to take effect.");
            System.out.println("      This session will use the installed FFmpeg directly.");

        } catch (Exception e) {
            throw new RuntimeException(
                    "FFmpeg installation failed: " + e.getMessage() + "\n" +
                            "Please install manually from: https://ffmpeg.org/download.html", e);
        }
    }

    // -------------------------------------------------------------------------
    // Availability check
    // -------------------------------------------------------------------------

    private boolean isFfmpegAvailable() {
        // Check system PATH
        try {
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor();
            if (p.exitValue() == 0) return true;
        } catch (Exception ignored) {}

        // Check our local install
        return localFfmpegBinary().exists();
    }

    private File localFfmpegBinary() {
        String binary = isWindows() ? "ffmpeg.exe" : "ffmpeg";
        return new File(INSTALL_DIR, binary);
    }

    // -------------------------------------------------------------------------
    // Platform installs
    // -------------------------------------------------------------------------

    private void downloadAndInstallWindows() throws IOException {
        File installDir = new File(INSTALL_DIR);
        installDir.mkdirs();

        File zipFile = new File(installDir, "ffmpeg.zip");
        downloadFile(DOWNLOAD_URL_WINDOWS, zipFile);

        System.out.println("Extracting...");
        extractZip(zipFile, installDir);
        zipFile.delete();
        flattenSingleSubdir(installDir);

        // Binary lives in bin/ffmpeg.exe — move it to INSTALL_DIR root for simplicity
        File binDir = new File(installDir, "bin");
        if (binDir.exists()) {
            for (File f : binDir.listFiles()) {
                f.renameTo(new File(installDir, f.getName()));
            }
            binDir.delete();
        }
    }

    private void downloadAndInstallLinux() throws IOException {
        File installDir = new File(INSTALL_DIR);
        installDir.mkdirs();

        File archive = new File(installDir, "ffmpeg.tar.xz");
        downloadFile(DOWNLOAD_URL_LINUX, archive);

        System.out.println("Extracting...");
        extractTarXz(archive, installDir);
        archive.delete();
        flattenSingleSubdir(installDir);

        // Make binary executable
        localFfmpegBinary().setExecutable(true);
    }

    private void downloadAndInstallMac() throws IOException {
        File installDir = new File(INSTALL_DIR);
        installDir.mkdirs();

        File zipFile = new File(installDir, "ffmpeg.zip");
        downloadFile(DOWNLOAD_URL_MAC, zipFile);

        System.out.println("Extracting...");
        extractZip(zipFile, installDir);
        zipFile.delete();
        flattenSingleSubdir(installDir);

        localFfmpegBinary().setExecutable(true);
    }

    // -------------------------------------------------------------------------
    // PATH registration
    // -------------------------------------------------------------------------

    private void addToPath() throws IOException, InterruptedException {
        if (isWindows()) {
            addToPathWindows();
        } else {
            addToPathUnix();
        }
    }

    private void addToPathWindows() throws IOException, InterruptedException {
        String newPath = INSTALL_DIR;

        String[] getCmd = {"reg", "query", "HKCU\\Environment", "/v", "PATH"};
        Process getProcess = new ProcessBuilder(getCmd).start();
        String output = new String(getProcess.getInputStream().readAllBytes());
        getProcess.waitFor();

        String currentPath = "";
        for (String line : output.split("\n")) {
            line = line.trim();
            if (line.startsWith("PATH")) {
                currentPath = line.replaceFirst("PATH\\s+REG_(SZ|EXPAND_SZ)\\s+", "").trim();
                break;
            }
        }

        if (currentPath.contains(newPath)) return;

        String updatedPath = currentPath.isEmpty() ? newPath : currentPath + ";" + newPath;
        String[] setCmd = {
                "reg", "add", "HKCU\\Environment",
                "/v", "PATH", "/t", "REG_EXPAND_SZ",
                "/d", updatedPath, "/f"
        };
        new ProcessBuilder(setCmd).start().waitFor();
        System.out.println("FFmpeg added to user PATH (Windows registry).");
    }

    private void addToPathUnix() throws IOException {
        String newPath = "export PATH=\"" + INSTALL_DIR + ":$PATH\"";

        // Detect shell config file
        String shell = System.getenv("SHELL");
        String rcFile;
        if (shell != null && shell.contains("zsh")) {
            rcFile = System.getProperty("user.home") + "/.zshrc";
        } else {
            rcFile = System.getProperty("user.home") + "/.bashrc";
        }

        File rc = new File(rcFile);

        // Don't add if already present
        if (rc.exists()) {
            String content = new String(Files.readAllBytes(rc.toPath()));
            if (content.contains(INSTALL_DIR)) {
                return;
            }
        }

        try (FileWriter fw = new FileWriter(rc, true)) {
            fw.write("\n# Added by TimeLapseCreator\n");
            fw.write(newPath + "\n");
        }
        System.out.println("FFmpeg added to PATH in " + rcFile);
    }

    // -------------------------------------------------------------------------
    // Download
    // -------------------------------------------------------------------------

    private void downloadFile(String urlString, File destination) throws IOException {
        System.out.println("Downloading FFmpeg from " + urlString);
        URL url = new URL(urlString);
        try (InputStream in = url.openStream();
             FileOutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
                System.out.printf("\r  Downloaded: %.1f MB", totalBytes / (1024.0 * 1024.0));
            }
            System.out.println();
        }
    }

    // -------------------------------------------------------------------------
    // Extraction
    // -------------------------------------------------------------------------

    private void extractZip(File zipFile, File targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(targetDir, entry.getName());
                if (!outFile.getCanonicalPath().startsWith(targetDir.getCanonicalPath())) {
                    throw new IOException("Zip slip detected: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    outFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) fos.write(buffer, 0, len);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private void extractTarXz(File archive, File targetDir) throws IOException {
        try (FileInputStream fis = new FileInputStream(archive);
             XZCompressorInputStream xzIn = new XZCompressorInputStream(fis);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(xzIn)) {

            TarArchiveEntry entry;
            while ((entry = tarIn.getNextTarEntry()) != null) {
                File outFile = new File(targetDir, entry.getName());
                if (!outFile.getCanonicalPath().startsWith(targetDir.getCanonicalPath())) {
                    throw new IOException("Tar slip detected: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    outFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = tarIn.read(buffer)) > 0) fos.write(buffer, 0, len);
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** If the target dir contains exactly one subdirectory, move its contents up. */
    private void flattenSingleSubdir(File dir) {
        File[] subDirs = dir.listFiles(File::isDirectory);
        if (subDirs == null || subDirs.length != 1) return;
        File sub = subDirs[0];
        File[] contents = sub.listFiles();
        if (contents == null) return;
        for (File f : contents) {
            f.renameTo(new File(dir, f.getName()));
        }
        sub.delete();
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }
}