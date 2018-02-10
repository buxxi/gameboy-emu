package se.omfilm.gameboy.cli;

import org.apache.commons.cli.*;
import se.omfilm.gameboy.Gameboy;
import se.omfilm.gameboy.internal.PPU.Shade;
import se.omfilm.gameboy.internal.memory.ROM;
import se.omfilm.gameboy.io.color.ColorPalette;
import se.omfilm.gameboy.io.color.FixedColorPalette;
import se.omfilm.gameboy.io.color.MultiColorPalette;
import se.omfilm.gameboy.io.controller.GLFWCompositeController;
import se.omfilm.gameboy.io.screen.GLFWScreen;
import se.omfilm.gameboy.io.screen.Screen;
import se.omfilm.gameboy.io.serial.ConsoleSerialConnection;
import se.omfilm.gameboy.io.serial.NullSerialConnection;
import se.omfilm.gameboy.io.serial.SerialConnection;
import se.omfilm.gameboy.io.sound.JavaSoundPlayback;
import se.omfilm.gameboy.io.sound.NullSoundPlayback;
import se.omfilm.gameboy.io.sound.ResampledSoundPlayback;
import se.omfilm.gameboy.io.sound.SoundPlayback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class Main {
    private static final String SPEED_ARG = "speed";
    private static final String BOOT_ARG = "boot";
    private static final String HELP_ARG = "help";
    private static final String ROM_ARG = "rom";
    private static final String PALETTE_ARG = "palette";
    private static final String SCREEN_ARG = "screen";
    private static final String RESAMPLE_ARG = "resample";
    private static final String MUTE_ARG = "mute";
    private static final String SERIAL_ARG = "serial";

    public static void main(String[] args) throws IOException, ParseException {
        Options options = new Options();
        options.addOption("h", HELP_ARG, false, "Prints this message");
        options.addOption("b", BOOT_ARG, true, "Path to the boot ROM");
        options.addRequiredOption("r", ROM_ARG, true, "Path to the ROM");
        options.addOption("p", PALETTE_ARG, true, "The color palette to use");
        options.addOption("sp", SPEED_ARG, true, "The speed to limit the emulator to");
        options.addOption("sc", SCREEN_ARG, true, "The screen mode to display the emulator in");
        options.addOption("r", RESAMPLE_ARG, true, "The mode to use for resampling sound");
        options.addOption("m", MUTE_ARG, false, "If the emulator shouldn't produce any sound");
        options.addOption("se", SERIAL_ARG, false, "If the emulator should output serial data to console");
        CommandLineParser parser = new DefaultParser();
        CommandLine result = parser.parse(options, args);

        if (result.hasOption("help")) {
            new HelpFormatter().printHelp("java -jar gameboy.jar", options);
        } else {
            run(result);
        }
    }

    private static void run(CommandLine cli) throws IOException {
        Path romPath = Paths.get(cli.getOptionValue(ROM_ARG));
        Path savePath = romPath.resolveSibling(romPath.getFileName().toString() + ".sav");

        ROM rom = ROM.load(Files.readAllBytes(romPath)).saveRAM(savePath);
        rom.print();

        ColorPalette palette = parsePalette(cli.getOptionValue(PALETTE_ARG, FixedColorPalette.PRESET.ORIGINAL_GREEN.toString()));
        GLFWScreen.Mode mode = GLFWScreen.Mode.valueOf(cli.getOptionValue(SCREEN_ARG, GLFWScreen.Mode.SCALE_4X.toString()));
        Gameboy.Speed speed = Gameboy.Speed.valueOf(cli.getOptionValue(SPEED_ARG, Gameboy.Speed.NORMAL.toString()));
        GLFWCompositeController controller = new GLFWCompositeController();
        Screen screen = new GLFWScreen(rom.name(), controller, mode);
        SoundPlayback sound = createSound(cli, speed);
        SerialConnection serial = createSerial(cli);

        Gameboy gameboy = new Gameboy(screen, palette, controller, serial, sound, rom, speed, false);
        if (cli.hasOption(BOOT_ARG)) {
            Path bootPath = Paths.get(cli.getOptionValue(BOOT_ARG));
            gameboy = gameboy.withBootData(Files.readAllBytes(bootPath));
        } else {
            gameboy.reset();
        }
        gameboy.run();
    }

    private static SerialConnection createSerial(CommandLine cli) {
        if (cli.hasOption(SERIAL_ARG)) {
            return new ConsoleSerialConnection();
        } else {
            return new NullSerialConnection();
        }
    }

    private static SoundPlayback createSound(CommandLine cli, Gameboy.Speed speed) {
        if (speed != Gameboy.Speed.HALF && !cli.hasOption(MUTE_ARG)) { //As long as we're playing it on normal or faster speed we can just drop samples
            ResampledSoundPlayback.Filter filter = ResampledSoundPlayback.Filter.valueOf(cli.getOptionValue(RESAMPLE_ARG, ResampledSoundPlayback.Filter.FLAT.toString()));
            return new ResampledSoundPlayback(new JavaSoundPlayback(), filter);
        }
        return new NullSoundPlayback();
    }

    private static ColorPalette parsePalette(String arg) {
        String[] parts = arg.split(",");
        if (parts.length == 1) {
            if (arg.equals("ALL_RANDOM")) {
                return new FixedColorPalette(
                        randomPalette().background(Shade.DARKEST),
                        randomPalette().background(Shade.DARK),
                        randomPalette().background(Shade.LIGHT),
                        randomPalette().background(Shade.LIGHTEST)
                );
            } else if (arg.equals("RANDOM")) {
                return randomPalette();
            }
            return FixedColorPalette.PRESET.valueOf(arg).getPalette();
        } else if (parts.length == 4) {
            return new MultiColorPalette(parsePalette(parts[0]), parsePalette(parts[1]), parsePalette(parts[2]), parsePalette(parts[3]));
        } else {
            throw new IllegalArgumentException("Can't handle " + arg + " as palette");
        }
    }

    private static ColorPalette randomPalette() {
        FixedColorPalette.PRESET[] values = FixedColorPalette.PRESET.values();
        return values[new Random().nextInt(values.length)].getPalette();
    }
}
