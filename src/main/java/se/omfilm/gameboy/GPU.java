package se.omfilm.gameboy;

import se.omfilm.gameboy.ByteArrayMemory;
import se.omfilm.gameboy.Interrupts;
import se.omfilm.gameboy.Memory;
import se.omfilm.gameboy.io.screen.Screen;
import se.omfilm.gameboy.util.DebugPrinter;

import java.awt.*;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GPU implements Memory {
    private static final int TILE_MAP_ADDRESS_0 = 0x9800;
    private static final int TILE_MAP_ADDRESS_1 = 0x9C00;
    private static final int TILE_DATA_ADDRESS_0 = 0x8800;
    private static final int TILE_DATA_ADDRESS_1 = 0x8000;

    private final Memory videoRam;
    private final Memory objectAttributeMemory;
    private final Screen screen;
    private final Interrupts interrupts;

    private final Tile[] tilesAddress0 = IntStream.range(0, 256).mapToObj(i -> new Tile(i, TILE_DATA_ADDRESS_0)).toArray(Tile[]::new);
    private final Tile[] tilesAddress1 = IntStream.range(0, 256).mapToObj(i -> new Tile(i, TILE_DATA_ADDRESS_1)).toArray(Tile[]::new);
    private Tile[] currentTiles = tilesAddress0;

    private final Sprite[] sprites = IntStream.range(0, 40).mapToObj(i -> new Sprite(i)).toArray(Sprite[]::new);

    private GPUMode mode = GPUMode.HBLANK;
    private int scrollX = 0;
    private int scrollY = 0;
    private int windowY = 0;
    private int windowX;
    private int backgroundPaletteData;
    private int objectPalette0Data;
    private int objectPalette1Data;
    private int cycleCounter = 0;
    private int scanline = 0;

    private boolean lcdDisplay = false;
    private int windowTileMapAddress = TILE_MAP_ADDRESS_0;
    private boolean windowDisplay = false;
    private int backgroundTileMapAddress = TILE_MAP_ADDRESS_0;
    private boolean largeSprites = false;
    private boolean spriteDisplay = false;
    private boolean backgroundDisplay = false;

    private boolean coincidence = false;
    private boolean oamInterrupt = false;
    private boolean vblankInterrupt = false;
    private boolean hblankInterrupt = false;

    public GPU(Screen screen, Interrupts interrupts) {
        this.interrupts = interrupts;
        this.videoRam = new ByteArrayMemory(Memory.MemoryType.VIDEO_RAM.allocate());
        this.objectAttributeMemory = new ByteArrayMemory(MemoryType.OBJECT_ATTRIBUTE_MEMORY.allocate());
        this.screen = screen;
    }

    public void step(int cycles) {
        if (!lcdDisplay) {
            return;
        }
        cycleCounter += cycles;

        if (cycleCounter <= mode.minimumCycles) {
            return;
        }

        cycleCounter = 0;

        switch (mode) {
            case HBLANK:
                scanline++;
                if (scanline == Screen.HEIGHT) {
                    mode = GPUMode.VBLANK;
                    interrupts.request(Interrupts.Interrupt.VBLANK);
                }
                mode = GPUMode.OAM;
                break;
            case VBLANK:
                scanline = 0;
                mode = GPUMode.OAM;
                break;
            case OAM:
                mode = GPUMode.VRAM;
                break;
            case VRAM:
                mode = GPUMode.HBLANK;
                if (scanline > (Screen.HEIGHT + 10)) {
                    scanline = 0;
                } else if (scanline <= Screen.HEIGHT) {
                    drawScanline();
                } else if (scanline == (Screen.HEIGHT + 1)) {
                    drawToScreen();
                }
                break;
        }
    }

    private void drawToScreen() {
        screen.draw();
    }

    private void drawScanline() {
        if (backgroundDisplay) {
            drawTiles();
        }
        if (spriteDisplay) {
            drawSprites();
        }
    }

    private void drawTiles() {
        if (windowDisplay) {
            throw new UnsupportedOperationException("Window rendering not implemented");
        }

        for (int i = 0; i < Screen.WIDTH; i++) {
            int y = scanline + scrollY;
            int x = i + scrollX;
            Tile tileNumber = resolveTile(y, x);
            Color color = color(tileNumber.getColorData(x, y)); //TODO: handle palette to get the correct color
            screen.setPixel(x, scanline - 1, color);
        }
    }

    private void drawSprites() {
        System.out.println(Arrays.stream(sprites).filter(s -> s.isOnScanline(scanline)).collect(Collectors.toList()));
        throw new UnsupportedOperationException("drawSprites() not implemented");
    }

    private Tile resolveTile(int y, int x) {
        int address = backgroundTileMapAddress - MemoryType.VIDEO_RAM.from + ((y / 8) * 32) + (x / 8); //Each row contains 32 tiles
        int id = videoRam.readByte(address);
        return currentTiles[id];
    }

    public int readByte(int address) {
        if (!mode.accessVideoRAM) {
            throw new IllegalStateException("The CPU can't access VideoRAM while in mode " + mode);
        }
        return videoRam.readByte(address);
    }

    public void writeByte(int address, int data) {
        MemoryType type = MemoryType.fromAddress(address);
        switch (type) {
            case VIDEO_RAM:
                if (!mode.accessVideoRAM) {
                    throw new IllegalStateException("The CPU can't access VideoRAM while in mode " + mode);
                }
                videoRam.writeByte(address - type.from, data);
                return;
            case OBJECT_ATTRIBUTE_MEMORY:
                if (mode.accessOAM) {
                    throw new IllegalStateException("The CPU can't access OAM while in mode " + mode);
                }
                objectAttributeMemory.writeByte(address - type.from, data);
                return;
            default:
                throw new UnsupportedOperationException("Can't write to address " + DebugPrinter.hex(address, 4) + " in " + getClass().getSimpleName());
        }

    }

    public void scrollY(int data) {
        this.scrollY = data;
    }

    public void scrollX(int data) {
        this.scrollX = data;
    }

    public int scrollY() {
        return scrollY;
    }

    public void windowY(int data) {
        this.windowY = data;
    }

    public void windowX(int data) {
        this.windowX = data;
    }

    public void setBackgroundPaletteData(int backgroundPaletteData) {
        this.backgroundPaletteData = backgroundPaletteData;
    }

    public void setObjectPalette0Data(int data) {
        this.objectPalette0Data = data;
    }

    public void setObjectPalette1Data(int data) {
        this.objectPalette1Data = data;
    }

    public void setLCDControl(int data) {
        lcdDisplay =                (data & 0b1000_0000) != 0;
        windowTileMapAddress =      (data & 0b0100_0000) != 0 ? TILE_MAP_ADDRESS_1 : TILE_MAP_ADDRESS_0;
        windowDisplay =             (data & 0b0010_0000) != 0;
        currentTiles =              (data & 0b0001_0000) != 0 ? tilesAddress1 : tilesAddress0;
        backgroundTileMapAddress =  (data & 0b0000_1000) != 0 ? TILE_MAP_ADDRESS_1 : TILE_MAP_ADDRESS_0;
        largeSprites =              (data & 0b0000_0100) != 0;
        spriteDisplay =             (data & 0b0000_0010) != 0;
        backgroundDisplay =         (data & 0b0000_0001) != 0;

        if (lcdDisplay && !screen.isOn()) {
            screen.turnOn();
        }
    }

    public void setInterruptEnables(int data) {
        coincidence =       (data & 0b0100_0000) != 0;
        oamInterrupt =      (data & 0b0010_0000) != 0;
        vblankInterrupt =   (data & 0b0001_0000) != 0;
        hblankInterrupt =   (data & 0b0000_1000) != 0;

        if (coincidence || oamInterrupt || vblankInterrupt || hblankInterrupt) {
            throw new UnsupportedOperationException("Unhandled value for setInterruptsEnables " + DebugPrinter.hex(data, 4));
        }
    }

    public int scanline() {
        return scanline;
    }

    private Color color(int input) {
        switch (input) {
            case 0:
            default:
                return Color.WHITE;
            case 1:
                return Color.LIGHT_GRAY;
            case 2:
                return Color.DARK_GRAY;
            case 3:
                return Color.BLACK;
        }
    }

    public void transferDMA(int offset, Memory ram) {
        for (int i = 0; i < MemoryType.OBJECT_ATTRIBUTE_MEMORY.size(); i++) {
            objectAttributeMemory.writeByte(i, ram.readByte(offset + i));
        }
    }

    private class Tile {
        private final int tileNumber;
        private final int dataAddress;

        public Tile(int tileNumber, int dataAddress) {
            this.tileNumber = tileNumber;
            this.dataAddress = dataAddress;
        }

        public int getColorData(int x, int y) {
            int rowData = resolveRowData(tileNumber, y);
            int colorBit = 1 << (7 - x % 8);
            return ((rowData & colorBit) != 0 ? 0b10 : 0b00) | (((rowData >> 8) & colorBit) != 0 ? 0b01 : 0b00);
        }

        private int resolveRowData(int tileNumber, int y) {
            int tileLocation = dataAddress - MemoryType.VIDEO_RAM.from + (tileNumber * 8 * 2); //Since each tile is 2 bytes and 8 rows long
            return videoRam.readWord(tileLocation + ((y % 8) * 2));
        }
    }

    private class Sprite {
        private final int spriteNum;

        public Sprite(int spriteNum) {
            this.spriteNum = spriteNum;
        }

        public boolean isOnScanline(int scanline) {
            if (largeSprites) {
                throw new UnsupportedOperationException("Not handling large sprites now");
            }

            return false;
        }

        private int x() {
            return objectAttributeMemory.readByte((spriteNum * 4) + 1) - 8;
        }

        private int y() {
            return objectAttributeMemory.readByte(spriteNum * 4) - 16;
        }
    }

    private enum GPUMode {
        HBLANK(0b00, true, true, 204),
        VBLANK(0b01, true, true, 456),
        OAM(0b10, false, true, 80),
        VRAM(0b11, false, false, 172);

        private final int bitMask;
        private final boolean accessOAM;
        private final boolean accessVideoRAM;
        private int minimumCycles;

        GPUMode(int bitMask, boolean accessOAM, boolean accessVideoRAM, int minimumCycles) {
            this.bitMask = bitMask;
            this.accessOAM = accessOAM;
            this.accessVideoRAM = accessVideoRAM;
            this.minimumCycles = minimumCycles;
        }
    }
}
