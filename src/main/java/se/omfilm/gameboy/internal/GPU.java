package se.omfilm.gameboy.internal;

import se.omfilm.gameboy.internal.memory.ByteArrayMemory;
import se.omfilm.gameboy.internal.memory.Memory;
import se.omfilm.gameboy.io.screen.Screen;
import se.omfilm.gameboy.util.DebugPrinter;

import java.awt.*;
import java.util.Arrays;
import java.util.stream.IntStream;

public class GPU implements Memory {
    private static final int TILE_MAP_ADDRESS_0 = 0x9800;
    private static final int TILE_MAP_ADDRESS_1 = 0x9C00;
    private static final int TILE_DATA_ADDRESS_0 = 0x8800;
    private static final int TILE_DATA_ADDRESS_1 = 0x8000;

    private final Memory videoRam;
    private final Screen screen;
    private final Interrupts interrupts;

    private final Tile[] tilesAddress0 = IntStream.range(0, 256).mapToObj(i -> new Tile(i, TILE_DATA_ADDRESS_0)).toArray(Tile[]::new);
    private final Tile[] tilesAddress1 = IntStream.range(0, 256).mapToObj(i -> new Tile(i, TILE_DATA_ADDRESS_1)).toArray(Tile[]::new);
    private Tile[] currentTiles = tilesAddress0;

    private final Sprite[] sprites = IntStream.range(0, 40).mapToObj(Sprite::new).toArray(Sprite[]::new);

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
    private int compareWithScanline = 0;

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
        this.screen = screen;
    }

    public void step(int cycles) {
        if (!updateCurrentMode()) {
            return;
        }

        cycleCounter -= cycles;

        if (cycleCounter <= 0) {
            scanline++;

            cycleCounter = GPUMode.VBLANK.minimumCycles;

            if (scanline == Screen.HEIGHT) {
                interrupts.request(Interrupts.Interrupt.VBLANK);
            } else if (scanline >= Screen.HEIGHT + 10) {
                scanline = 0;
                drawToScreen();
            } else if (scanline < Screen.HEIGHT) {
                drawScanline();
            }
        }
    }

    private boolean updateCurrentMode() {
        if (!lcdDisplay) {
            cycleCounter = GPUMode.VBLANK.minimumCycles;
            mode = GPUMode.VBLANK;
            scanline = 0;
            return false;
        }

        if (scanline >= Screen.HEIGHT) {
            updateCurrentMode(GPUMode.VBLANK, vblankInterrupt);
        } else if (cycleCounter >= GPUMode.VBLANK.minimumCycles - GPUMode.OAM.minimumCycles) {
            updateCurrentMode(GPUMode.OAM, oamInterrupt);
        } else if (cycleCounter >= GPUMode.VBLANK.minimumCycles - GPUMode.OAM.minimumCycles - GPUMode.VRAM.minimumCycles) {
            updateCurrentMode(GPUMode.VRAM, false);
        } else {
            updateCurrentMode(GPUMode.HBLANK, hblankInterrupt);
        }

        if (coincidence && scanline == compareWithScanline) {
            interrupts.request(Interrupts.Interrupt.LCD);
        }

        return true;
    }

    private void updateCurrentMode(GPUMode newMode, boolean requestInterrupt) {
        if (requestInterrupt && newMode != mode) {
            interrupts.request(Interrupts.Interrupt.LCD);
        }
        mode = newMode;
    }

    private void drawToScreen() {
        screen.draw();
    }

    private void drawScanline() {
        if (backgroundDisplay) {
            for (int x = 0; x < Screen.WIDTH; x++) {
                if (windowDisplay && windowY <= scanline) {
                    drawBackgroundWindowPixel(x);
                } else {
                    drawBackgroundPixel(x);
                }
            }
        }
        if (spriteDisplay) {
            drawSprites();
        }
    }

    private void drawBackgroundWindowPixel(int x) {
        int y = scanline - windowY;
        if (x >= windowX) {
            x -= windowX;
        }
        drawPixel(x, y, windowTileMapAddress);
    }

    private void drawBackgroundPixel(int x) {
        int y = (scanline + scrollY) & 0xFF;
        x = (x + scrollX) & 0xFF;
        drawPixel(x, y, backgroundTileMapAddress);
    }

    private void drawPixel(int x, int y, int tileMapAddress) {
        Tile tileNumber = resolveTile(y, x, tileMapAddress);
        Color color = color(tileNumber.getColorData(x, y), backgroundPaletteData);
        screen.setPixel(x, scanline - 1, color);
    }

    private void drawSprites() {
        Arrays.stream(sprites).filter(s -> s.isOnScanline(scanline)).forEach(s -> s.renderOn(scanline));
    }

    private Tile resolveTile(int y, int x, int tileMapAddress) {
        int address = tileMapAddress - MemoryType.VIDEO_RAM.from + ((y / 8) * 32) + (x / 8); //Each row contains 32 tiles
        int id = videoRam.readByte(address);
        if (currentTiles == tilesAddress0) {
            id = 128 + ((byte) id);
        }
        return currentTiles[id];
    }

    public int readByte(int address) {
        MemoryType type = MemoryType.fromAddress(address);
        switch (type) {
            case VIDEO_RAM:
                return videoRam.readByte(address - type.from);
            case OBJECT_ATTRIBUTE_MEMORY:
                //TODO: do I need to implement this?
            default:
                throw new UnsupportedOperationException("Can't read from address " + DebugPrinter.hex(address, 4) + " in " + getClass().getSimpleName());
        }
    }

    public void writeByte(int address, int data) {
        MemoryType type = MemoryType.fromAddress(address);
        switch (type) {
            case VIDEO_RAM:
                videoRam.writeByte(address - type.from, data);
                return;
            case OBJECT_ATTRIBUTE_MEMORY:
                int virtualAddress = address - type.from;
                writeOAMByte(virtualAddress, data);
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

    public int getLCDControl() {
        return  (lcdDisplay ?                                       0b1000_0000 : 0) |
                (windowTileMapAddress == TILE_MAP_ADDRESS_1 ?       0b0100_0000 : 0) |
                (windowDisplay ?                                    0b0010_0000 : 0) |
                (currentTiles == tilesAddress1 ?                    0b0001_0000 : 0) |
                (backgroundTileMapAddress == TILE_MAP_ADDRESS_1 ?   0b0000_1000 : 0) |
                (largeSprites ?                                     0b0000_0100 : 0) |
                (spriteDisplay ?                                    0b0000_0010 : 0) |
                (backgroundDisplay ?                                0b0000_0001 : 0);

    }

    public void setInterruptEnables(int data) {
        coincidence =       (data & 0b0100_0000) != 0;
        oamInterrupt =      (data & 0b0010_0000) != 0;
        vblankInterrupt =   (data & 0b0001_0000) != 0;
        hblankInterrupt =   (data & 0b0000_1000) != 0;
    }

    public int getLCDStatus() {
        return  (coincidence ?                      0b0100_0000 : 0) |
                (oamInterrupt ?                     0b0010_0000 : 0) |
                (vblankInterrupt ?                  0b0001_0000 : 0) |
                (hblankInterrupt ?                  0b0000_1000 : 0) |
                (scanline == compareWithScanline ?  0b0000_0100 : 0) |
                mode.id;
    }

    public int scanline() {
        return scanline;
    }

    public int scanlineCompare() {
        return compareWithScanline;
    }

    public void scanlineCompare(int compareWithScanline) {
        this.compareWithScanline = compareWithScanline;
    }

    private Color color(int input, int palette) {
        int offset = input * 2;
        int mask = (0b0000_0011 << offset);
        int result = (palette & mask) >> offset;

        switch (result) {
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
            writeOAMByte(i, ram.readByte(offset + i));
        }
    }

    private void writeOAMByte(int virtualAddress, int value) {
        int spriteNumber = virtualAddress / 4;
        int type = virtualAddress % 4;

        Sprite sprite = sprites[spriteNumber];
        switch (type) {
            case 0:
                sprite.y = value - 16;
                break;
            case 1:
                sprite.x = value - 8;
                break;
            case 2:
                sprite.tileNumber = value;
                break;
            case 3:
                sprite.prioritizeSprite =   (value & 0b1000_0000) == 0;
                sprite.flipY =              (value & 0b0100_0000) != 0;
                sprite.flipX =              (value & 0b0010_0000) != 0;
                sprite.colorPalette =       (value & 0b0001_0000) == 0 ? objectPalette0Data : objectPalette1Data;

                if (!sprite.prioritizeSprite) {
                    throw new UnsupportedOperationException("Prioritization of background not implemented");
                }
        }
    }

    private class Tile {
        private final int tileNumber;
        private final int dataAddress;

        private Tile(int tileNumber, int dataAddress) {
            this.tileNumber = tileNumber;
            this.dataAddress = dataAddress;
        }

        private int getColorData(int x, int y) {
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
        private int x = 0;
        private int y = 0;
        private int tileNumber = 0;
        private boolean prioritizeSprite = true;
        private boolean flipY = false;
        private boolean flipX = false;
        private int colorPalette = objectPalette0Data;

        private Sprite(int spriteNum) {
        }

        private boolean isOnScanline(int scanline) {
            int y = this.y;
            return scanline >= y && scanline < (y + (largeSprites ? 16 : 8));
        }

        private void renderOn(int scanline) {
            Tile tile = tilesAddress1[this.tileNumber];

            for (int i = 0; i < 8; i++) {
                int x = (this.x + i % 8);
                int y = scanline - this.y;
                if (flipX) {
                    x = 8 - x;
                }
                if (flipY) {
                    y = (largeSprites ? 16 : 8) - y;
                }

                Color color = color(tile.getColorData(x, y), colorPalette);
                screen.setPixel(this.x + i, scanline - 1, color);
            }
        }
    }

    private enum GPUMode {
        HBLANK(0, 204),
        VBLANK(1, 456),
        OAM(2, 80),
        VRAM(3, 172);

        private final int id;
        private int minimumCycles;

        GPUMode(int id, int minimumCycles) {
            this.id = id;
            this.minimumCycles = minimumCycles;
        }
    }
}
