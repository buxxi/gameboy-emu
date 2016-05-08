package se.omfilm.gameboy.internal;

import se.omfilm.gameboy.internal.memory.Memory;
import se.omfilm.gameboy.io.screen.Screen;
import se.omfilm.gameboy.util.DebugPrinter;

import java.awt.*;
import java.util.Arrays;
import java.util.stream.IntStream;

public class GPU implements Memory {
    private static final int TILE_MAP_ADDRESS_0 = 0x9800;
    private static final int TILE_MAP_ADDRESS_1 = 0x9C00;

    private final Screen screen;
    private final Interrupts interrupts;

    private Tile[] tiles = IntStream.range(0, 384).mapToObj(Tile::new).toArray(Tile[]::new);
    private int tileOffset = 0;
    private int[][] tileMap0 = new int[32][32];
    private int[][] tileMap1 = new int[32][32];
    private int[][] windowTileMap = tileMap0;
    private int[][] backgroundTileMap = tileMap0;

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
    private boolean windowDisplay = false;
    private boolean largeSprites = false;
    private boolean spriteDisplay = false;
    private boolean backgroundDisplay = false;

    private boolean coincidence = false;
    private boolean oamInterrupt = false;
    private boolean vblankInterrupt = false;
    private boolean hblankInterrupt = false;

    public GPU(Screen screen, Interrupts interrupts) {
        this.interrupts = interrupts;
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
        drawPixel(x, y, windowTileMap);
    }

    private void drawBackgroundPixel(int x) {
        int y = (scanline + scrollY) & 0xFF;
        x = (x + scrollX) & 0xFF;
        drawPixel(x, y, backgroundTileMap);
    }

    private void drawPixel(int x, int y, int[][] tileMap) {
        int id = tileMap[y / 8][x / 8];
        if (tileOffset != 0) {
            id = tileOffset + ((byte) id);
        }
        Tile tileNumber = tiles[id];
        Color color = color(tileNumber.getColorData(x, y), backgroundPaletteData);
        screen.setPixel(x, scanline - 1, color);
    }

    private void drawSprites() {
        Arrays.stream(sprites).filter(s -> s.isOnScanline(scanline)).forEach(s -> s.renderOn(scanline));
    }

    public int readByte(int address) {
        MemoryType type = MemoryType.fromAddress(address);
        switch (type) {
            case VIDEO_RAM:
                if (address >= TILE_MAP_ADDRESS_0) {
                    return readTileMapByte(address);
                } else {
                    return readTileByte(address - type.from);
                }
            case OBJECT_ATTRIBUTE_MEMORY:
                return readOAMByte(address - type.from);
            default:
                throw new UnsupportedOperationException("Can't read from address " + DebugPrinter.hex(address, 4) + " in " + getClass().getSimpleName());
        }
    }

    public void writeByte(int address, int data) {
        MemoryType type = MemoryType.fromAddress(address);
        switch (type) {
            case VIDEO_RAM:
                if (address >= TILE_MAP_ADDRESS_0) {
                    writeTileMapByte(address, data);
                } else {
                    writeTileByte(address - MemoryType.VIDEO_RAM.from, data);
                }
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
        windowTileMap =             (data & 0b0100_0000) != 0 ? tileMap1 : tileMap0;
        windowDisplay =             (data & 0b0010_0000) != 0;
        tileOffset =                (data & 0b0001_0000) != 0 ? 0 : 256;
        backgroundTileMap =         (data & 0b0000_1000) != 0 ? tileMap1 : tileMap0;
        largeSprites =              (data & 0b0000_0100) != 0;
        spriteDisplay =             (data & 0b0000_0010) != 0;
        backgroundDisplay =         (data & 0b0000_0001) != 0;

        if (lcdDisplay && !screen.isOn()) {
            screen.turnOn();
        }
    }

    public int getLCDControl() {
        return  (lcdDisplay ?                                       0b1000_0000 : 0) |
                (windowTileMap == tileMap1 ?                        0b0100_0000 : 0) |
                (windowDisplay ?                                    0b0010_0000 : 0) |
                (tileOffset == 0 ?                                  0b0001_0000 : 0) |
                (backgroundTileMap == tileMap1 ?                    0b0000_1000 : 0) |
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

    private void writeTileMapByte(int address, int value) {
        int[][] tileMap;
        if (address < TILE_MAP_ADDRESS_1) {
            address -= TILE_MAP_ADDRESS_0;
            tileMap = tileMap0;
        } else {
            address -= TILE_MAP_ADDRESS_1;
            tileMap = tileMap1;
        }

        tileMap[address / 32][address % 32] = value;
    }

    private void writeTileByte(int virtualAddress, int value) {
        Tile tile = tiles[virtualAddress / 16]; //Each tile uses 16 bytes
        int rowData = virtualAddress % 16;
        int y = rowData / 2; //Each row uses 2 bytes
        for (int x = 0; x < 8; x++) {
            int colorBit = 1 << (7 - x); //The x-coordinates are backwards
            if (rowData % 2 == 0) { //The 2 bytes for each row should be combined into a single value with the first bytes value in bit 1 and the second bytes value in bit 0
                tile.graphics[y][x] = ((value & colorBit) != 0 ? 0b01 : 0b00) | (tile.graphics[y][x] & 0b10);
            } else {
                tile.graphics[y][x] = ((value & colorBit) != 0 ? 0b10 : 0b00) | (tile.graphics[y][x] & 0b01);
            }
        }
    }

    private int readOAMByte(int virtualAddress) {
        int spriteNumber = virtualAddress / 4;
        int type = virtualAddress % 4;

        Sprite sprite = sprites[spriteNumber];
        switch (type) {
            case 0:
                return sprite.y + 16;
            case 1:
                return sprite.x + 8;
            case 2:
                return sprite.tileNumber;
            case 3:
                return  (!sprite.prioritizeSprite ?                     0b1000_0000 : 0) |
                        (sprite.flipY ?                                 0b0100_0000 : 0) |
                        (sprite.flipX ?                                 0b0010_0000 : 0) |
                        (sprite.colorPalette == objectPalette1Data ?    0b0001_0000 : 0);
        }
        throw new IllegalArgumentException("Unreachable code");
    }

    private int readTileMapByte(int address) {
        int[][] tileMap;
        if (address < TILE_MAP_ADDRESS_1) {
            address -= TILE_MAP_ADDRESS_0;
            tileMap = tileMap0;
        } else {
            address -= TILE_MAP_ADDRESS_1;
            tileMap = tileMap1;
        }

        return tileMap[address / 32][address % 32];
    }

    //TODO: can some code be reused from writeTileByte?
    private int readTileByte(int virtualAddress) {
        int result = 0;

        Tile tile = tiles[virtualAddress / 16];
        int rowData = virtualAddress % 16;
        int y = rowData / 2;
        for (int x = 0; x < 8; x++) {
            int colorBit = 1 << (7 - x);
            int pixel = tile.graphics[y][x];
            if (rowData % 2 == 0) {
                result = result | ((pixel & 0b01) != 0 ? colorBit : 0);
            } else {
                result = result | ((pixel & 0b10) != 0 ? colorBit : 0);
            }
        }
        return result;
    }

    private class Tile {
        private final int[][] graphics = new int[8][8];

        private Tile(int tileNumber) {
        }

        private int getColorData(int x, int y) {
            return graphics[y & 7][x & 7];
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
            Tile tile = tiles[this.tileNumber];

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
