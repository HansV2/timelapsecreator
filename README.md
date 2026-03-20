# TimeLapse Creator

A ("fancy") command-line tool that turns a folder of timestamped images into a timelapse video. Optionally generates a synchronized day-counter video alongside it.

---

## Features

- Converts a folder of `.jpg` images into an `.mp4` timelapse
- Each image is one frame — FPS is fully configurable
- Generates a synchronized day-counter video (default: white number on black background)
- Interactive setup wizard on launch — no config file editing needed
- Config is saved and can be reused on subsequent runs
- Automatically downloads and installs FFmpeg if not found on the system

---

## Requirements

- Java 11 or higher
- FFmpeg (will be downloaded automatically if missing)

---

## Installation

Download the executable for your platform from the [Releases](https://github.com/HansV2/timelapsecreator/releases) page:

| File | Platform |
|------|----------|
| `timelapsecreator.exe` | Windows |
| `timelapsecreator-linux` | Linux |
| `timelapsecreator-mac.command` | macOS |

No installation required — just place the executable in a folder and run it.

---

## Usage

Only tested on Windows.

1. Place the executable anywhere on your system
2. Run it — the setup wizard will guide you through all configuration options including where your images are located

The wizard runs every time and lets you either keep your saved configuration or change any setting before processing starts.

### Paths

All paths in the configuration can be **absolute** or **relative**. Relative paths are resolved from the directory the executable is run from. This means you can place the executable directly next to your image folder and use a relative path to that folder.

### Expected image filename format

Images must be `.jpg` files whose **entire filename** (without extension) matches the configured DateTime pattern exactly — no extra text before or after is supported. The filename is parsed as a `LocalDateTime` using Java's `DateTimeFormatter`, so the pattern must cover the full filename.

By default the expected pattern is:

```
yyyy-MM-dd_HH-mm-ss.jpg
```

Example: `2026-03-18_07-00-01.jpg`

Files that do not match the configured pattern are silently skipped. Images are sorted within each day by filename alphabetically, so the pattern should be structured such that alphabetical order equals chronological order (which the default pattern satisfies).

---

## Output

Per default, two videos are created next to the executable (paths are configurable):

| File | Description |
|------|-------------|
| `imageLapse.mp4` | The timelapse video |
| `numberLapse.mp4` | A synchronized video showing the day number for each frame |

The two videos are designed to be merged in a video editor — for example to overlay the day counter on top of the timelapse.

---

## Configuration

The wizard runs automatically on every launch. You are asked whether to use the saved config or reconfigure.

| Setting | Description | Default |
|---------|-------------|---------|
| Input images folder | Path to folder containing `.jpg` images | `raw` |
| Timelapse output folder | Where `imageLapse.mp4` is saved | `.` |
| Numbers video output folder | Where `numberLapse.mp4` is saved | `.` |
| Create both videos | Whether to also generate the day-counter video | `true` |
| FPS | Images shown per second of the output videos | `30` |
| Filename pattern | DateTime pattern used to parse image filenames | `yyyy-MM-dd_HH-mm-ss` |
| Skip days without images | If true, days with no images are omitted entirely (numberLapse-video also skips this day) | `false` |
| Numbers video width | Width of the day-counter video in pixels | `1920` |
| Numbers video height | Height of the numberLapse video in pixels | `1080` |
| Font size | Font size of the day number | `200` |
| Background color | Background color of the day-counter video | Black |
| Font color | Color of the day number text | White |
| Show FFmpeg log output | Whether to print FFmpeg's console output during processing | `false` |

Config is stored at:
- **Windows:** `C:\Users\<name>\.timelapsecreator\config.json`
- **Linux/macOS:** `~/.timelapsecreator/config.json`

---

## How days are counted

- Images are grouped by the **date** part of their filename
- Day **1** is the date of the earliest image
- Every calendar day between the first and last date is counted — even days with no images
- On days with no images, the last image of the previous day is held (unless "skip days without images" is enabled)
- If a day has more than the target FPS worth of images, they are evenly subsampled to fit

---

## Building from source

Requires Maven 3.9+ and JDK 11+.

```bash
git clone https://github.com/HansV2/timelapsecreator.git
cd timelapsecreator
mvn package
```

Outputs are placed in `target/`:
- `timelapsecreator.exe` — Windows executable
- `timelapsecreator-linux` — Linux executable
- `timelapsecreator-mac.command` — macOS executable

---

## FFmpeg

This tool uses [FFmpeg](https://ffmpeg.org/) to encode videos. If FFmpeg is not found on your system, the application will offer to download and install it automatically. On Windows it is added to your user PATH. On Linux/macOS the binary is placed in `~/.timelapsecreator/ffmpeg/` and the appropriate shell config (`.bashrc` or `.zshrc`) is updated.

---

## License

Apache 2.0

Copyright 2026 HansV2

---

## Disclosure

Parts of this project were developed with the assistance of AI tools, including [Claude](https://claude.ai) by Anthropic. All generated code was reviewed and integrated by the project author.
