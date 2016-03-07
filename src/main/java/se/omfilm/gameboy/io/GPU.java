package se.omfilm.gameboy.io;

import se.omfilm.gameboy.ByteArrayMemory;
import se.omfilm.gameboy.Memory;
import se.omfilm.gameboy.io.screen.Screen;
import se.omfilm.gameboy.util.DebugPrinter;

import java.awt.*;

public class GPU implements Memory {
    private static final int TILE_MAP_ADDRESS_0 = 0x9800;
    private static final int TILE_MAP_ADDRESS_1 = 0x9C00;
    private static final int TILE_DATA_ADDRESS_0 = 0x8800;
    private static final int TILE_DATA_ADDRESS_1 = 0x8000;

    private final Memory videoRam;
    private final Memory objectAttributeMemory;
    private final Screen screen;

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
    private int tileDataAddress = TILE_DATA_ADDRESS_0;
    private int backgroundTileMapAddress = TILE_MAP_ADDRESS_0;
    private boolean largeSprites = false;
    private boolean spriteDisplay = false;
    private boolean backgroundDisplay = false;

    public GPU(Screen screen) {
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
        for (int i = 0; i < Screen.WIDTH; i++) {
            int y = scanline + scrollY;
            int x = i + scrollX;
            int tileNumber = resolveTileNumber(y, x);
            int rowData = resolveRowData(tileNumber, y);
            Color color = color(colorData(rowData, x % 8)); //TODO: handle palette to get the correct color
            screen.setPixel(x, scanline - 1, color);
        }
    }

    private void drawSprites() {
        throw new UnsupportedOperationException("drawSprites() not implemented");
    }

    private int resolveRowData(int tileNumber, int y) {
        int tileLocation = tileDataAddress - MemoryType.VIDEO_RAM.from + (tileNumber * 8 * 2); //Since each tile is 2 bytes and 8 rows long
        return videoRam.readWord(tileLocation + ((y % 8) * 2));
    }

    private int resolveTileNumber(int y, int x) {
        int address = backgroundTileMapAddress - MemoryType.VIDEO_RAM.from + ((y / 8) * 32) + (x / 8); //Each row contains 32 tiles
        return videoRam.readByte(address);
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
        lcdDisplay = (data & 0b1000_0000) != 0;
        windowTileMapAddress = (data & 0b0100_0000) != 0 ? TILE_MAP_ADDRESS_1 : TILE_MAP_ADDRESS_0;
        windowDisplay = (data & 0b0010_0000) != 0;
        tileDataAddress = (data & 0b0001_0000) != 0 ? TILE_DATA_ADDRESS_1 : TILE_DATA_ADDRESS_0;
        backgroundTileMapAddress = (data & 0b0000_1000) != 0 ? TILE_MAP_ADDRESS_1 : TILE_MAP_ADDRESS_0;
        largeSprites = (data & 0b0000_0100) != 0;
        spriteDisplay = (data & 0b0000_0010) != 0;
        backgroundDisplay = (data & 0b0000_0001) != 0;

        if (lcdDisplay) {
            screen.turnOn();
        } else {
            screen.turnOff();
        }
    }

    public int scanline() {
        return scanline;
    }

    private int colorData(int rowData, int x) {
        int colorBit = 1 << (7 - x);
        return ((rowData & colorBit) != 0 ? 0b10 : 0b00) | (((rowData >> 8) & colorBit) != 0 ? 0b01 : 0b00);
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
