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
    private static final int TILE_MAP_WIDTH = 32;
    private static final int TILE_MAP_HEIGHT = 32;

    private static final int TILE_WIDTH = 8;
    private static final int TILE_HEIGHT = 8;
    private static final int TILE_COUNT = 384;
    private static final int TILE_BYTE_SIZE = 16;

    private static final int SPRITE_COUNT = 40;
    private static final int SPRITE_BYTE_SIZE = 4;
    private static final int SPRITE_HEIGHT = 16;
    private static final int SPRITE_WIDTH = 8;

    private final Screen screen;
    private final Interrupts interrupts;

    private Palette backgroundPaletteData = new Palette(0);
    private Palette objectPalette0Data = new Palette(0);
    private Palette objectPalette1Data = new Palette(0);

    private final Tile[] tiles = IntStream.range(0, TILE_COUNT).mapToObj(Tile::new).toArray(Tile[]::new);
    private final Sprite[] sprites = IntStream.range(0, SPRITE_COUNT).mapToObj(Sprite::new).toArray(Sprite[]::new);
    private int tileOffset = 0;
    private int[][] tileMap0 = new int[TILE_MAP_HEIGHT][TILE_MAP_WIDTH];
    private int[][] tileMap1 = new int[TILE_MAP_HEIGHT][TILE_MAP_WIDTH];
    private int[][] windowTileMap = tileMap0;
    private int[][] backgroundTileMap = tileMap0;

    private GPUMode mode = GPUMode.HBLANK;
    private int scrollX = 0;
    private int scrollY = 0;
    private int windowY = 0;
    private int windowX; //TODO: handle this too

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
        Color[] scanlineBuffer = new Color[Screen.WIDTH];
        if (backgroundDisplay) {
            for (int x = 0; x < Screen.WIDTH; x++) {
                if (windowDisplay && windowY <= scanline && windowX >= 7) {
                    drawBackgroundWindowPixel(scanlineBuffer, x);
                } else {
                    drawBackgroundPixel(scanlineBuffer, x);
                }
            }
        }
        if (spriteDisplay) {
            drawSprites(scanlineBuffer);
        }
        for (int x = 0; x < Screen.WIDTH; x++) {
            Color color = scanlineBuffer[x] != null ? scanlineBuffer[x] : Color.WHITE;
            screen.setPixel(x, scanline - 1, color);
        }
    }

    private void drawBackgroundWindowPixel(Color[] scanlineBuffer, int x) {
        int y = scanline + windowY;
        int adjustedX = x + windowX - 7;
        Tile tile = tileAt(adjustedX, y, windowTileMap);
        scanlineBuffer[x] = tile.colorAt(adjustedX, y, backgroundPaletteData);
    }

    private void drawBackgroundPixel(Color[] scanlineBuffer, int x) {
        int y = scanline + scrollY;
        int adjustedX = x + scrollX;
        Tile tile = tileAt(adjustedX, y, backgroundTileMap);
        scanlineBuffer[x] = tile.colorAt(adjustedX, y, backgroundPaletteData);
    }

    private void drawSprites(Color[] scanlineBuffer) {
        Arrays.stream(sprites).filter(Sprite::isOnScanline).forEach(s -> s.renderOn(scanlineBuffer));
    }

    private Tile tileAt(int x, int y, int[][] tileMap) {
        x = ((x / TILE_WIDTH) + TILE_MAP_WIDTH) % TILE_MAP_WIDTH;
        y = ((y / TILE_HEIGHT) + TILE_MAP_HEIGHT) % TILE_MAP_HEIGHT;
        int id = tileMap[y][x];
        if (tileOffset != 0) {
            id = tileOffset + ((byte) id);
        }
        return tiles[id];
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
        this.backgroundPaletteData = new Palette(backgroundPaletteData);
    }

    public void setObjectPalette0Data(int data) {
        this.objectPalette0Data = new Palette(data);
    }

    public void setObjectPalette1Data(int data) {
        this.objectPalette1Data = new Palette(data);
    }

    public void setLCDControl(int data) {
        lcdDisplay =        (data & 0b1000_0000) != 0;
        windowTileMap =     (data & 0b0100_0000) != 0 ? tileMap1 : tileMap0;
        windowDisplay =     (data & 0b0010_0000) != 0;
        tileOffset =        (data & 0b0001_0000) != 0 ? 0 : (0xFF + 1);
        backgroundTileMap = (data & 0b0000_1000) != 0 ? tileMap1 : tileMap0;
        largeSprites =      (data & 0b0000_0100) != 0;
        spriteDisplay =     (data & 0b0000_0010) != 0;
        backgroundDisplay = (data & 0b0000_0001) != 0;

        if (lcdDisplay && !screen.isOn()) {
            screen.turnOn();
        }
    }

    public int getLCDControl() {
        return  (lcdDisplay ?                       0b1000_0000 : 0) |
                (windowTileMap == tileMap1 ?        0b0100_0000 : 0) |
                (windowDisplay ?                    0b0010_0000 : 0) |
                (tileOffset == 0 ?                  0b0001_0000 : 0) |
                (backgroundTileMap == tileMap1 ?    0b0000_1000 : 0) |
                (largeSprites ?                     0b0000_0100 : 0) |
                (spriteDisplay ?                    0b0000_0010 : 0) |
                (backgroundDisplay ?                0b0000_0001 : 0);

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

    public void transferDMA(int offset, Memory ram) {
        for (int i = 0; i < MemoryType.OBJECT_ATTRIBUTE_MEMORY.size(); i++) {
            writeOAMByte(i, ram.readByte(offset + i));
        }
    }

    private void writeOAMByte(int virtualAddress, int value) {
        int spriteNumber = virtualAddress / SPRITE_BYTE_SIZE;
        int type = virtualAddress % SPRITE_BYTE_SIZE;

        Sprite sprite = sprites[spriteNumber];
        switch (type) {
            case 0:
                sprite.y = value - SPRITE_HEIGHT;
                break;
            case 1:
                sprite.x = value - SPRITE_WIDTH;
                break;
            case 2:
                sprite.tileNumber = value;
                break;
            case 3:
                sprite.prioritizeSprite =   (value & 0b1000_0000) == 0;
                sprite.flipY =              (value & 0b0100_0000) != 0;
                sprite.flipX =              (value & 0b0010_0000) != 0;
                sprite.palette =            (value & 0b0001_0000) == 0 ? objectPalette0Data : objectPalette1Data;
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

        tileMap[address / TILE_MAP_HEIGHT][address % TILE_MAP_HEIGHT] = value;
    }

    private void writeTileByte(int virtualAddress, int value) {
        Tile tile = tiles[virtualAddress / TILE_BYTE_SIZE]; //Each tile uses 16 bytes
        int rowData = virtualAddress % TILE_BYTE_SIZE;
        int y = rowData / 2; //Each row uses 2 bytes
        for (int x = 0; x < TILE_WIDTH; x++) {
            int colorBit = 1 << (TILE_WIDTH - 1 - x); //The x-coordinates are backwards
            if (rowData % 2 == 0) { //The 2 bytes for each row should be combined into a single value with the first bytes value in bit 1 and the second bytes value in bit 0
                tile.graphics[y][x] = ((value & colorBit) != 0 ? 0b01 : 0b00) | (tile.graphics[y][x] & 0b10);
            } else {
                tile.graphics[y][x] = ((value & colorBit) != 0 ? 0b10 : 0b00) | (tile.graphics[y][x] & 0b01);
            }
        }
    }

    private int readOAMByte(int virtualAddress) {
        int spriteNumber = virtualAddress / SPRITE_BYTE_SIZE;
        int type = virtualAddress % SPRITE_BYTE_SIZE;

        Sprite sprite = sprites[spriteNumber];
        switch (type) {
            case 0:
                return sprite.y + SPRITE_HEIGHT;
            case 1:
                return sprite.x + SPRITE_WIDTH;
            case 2:
                return sprite.tileNumber;
            case 3:
                return  (!sprite.prioritizeSprite ?                     0b1000_0000 : 0) |
                        (sprite.flipY ?                                 0b0100_0000 : 0) |
                        (sprite.flipX ?                                 0b0010_0000 : 0) |
                        (sprite.palette == objectPalette1Data ?         0b0001_0000 : 0);
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

        return tileMap[address / TILE_MAP_HEIGHT][address % TILE_MAP_HEIGHT];
    }

    private int readTileByte(int virtualAddress) {
        int result = 0;

        Tile tile = tiles[virtualAddress / TILE_BYTE_SIZE]; //Each tile uses 16 bytes
        int rowData = virtualAddress % TILE_BYTE_SIZE;
        int y = rowData / 2; //Each row uses 2 bytes
        for (int x = 0; x < TILE_WIDTH; x++) {
            int colorBit = 1 << (TILE_WIDTH - 1 - x);  //The x-coordinates are backwards
            int pixel = tile.graphics[y][x];
            if (rowData % 2 == 0) { //The 2 bytes for each row should be combined into a single value with the first bytes value in bit 1 and the second bytes value in bit 0
                result = result | ((pixel & 0b01) != 0 ? colorBit : 0);
            } else {
                result = result | ((pixel & 0b10) != 0 ? colorBit : 0);
            }
        }
        return result;
    }

    private class Tile {
        private final int tileNumber;

        private final int[][] graphics = new int[TILE_HEIGHT][TILE_WIDTH];

        private Tile(int tileNumber) {
            this.tileNumber = tileNumber;
        }

        private Color colorAt(int x, int y, Palette palette) {
            return palette.color(graphics[y % TILE_HEIGHT][x % TILE_WIDTH]);
        }

        @Override
        public String toString() {
            return "Tile(" + tileNumber + ")";
        }
    }

    private class Sprite {
        private final int spriteNum;

        private int x = 0;
        private int y = 0;
        private int tileNumber = 0;
        private boolean prioritizeSprite = true;
        private boolean flipY = false;
        private boolean flipX = false;
        private Palette palette = objectPalette0Data;

        private Sprite(int spriteNum) {
            this.spriteNum = spriteNum;
        }

        private boolean isOnScanline() {
            int y = this.y;
            return scanline >= y && scanline < (y + height());
        }

        private void renderOn(Color[] scanlineBuffer) {
            for (int i = 0; i < SPRITE_WIDTH; i++) {
                int x = i;
                int y = (scanline - this.y);
                if (flipX) {
                    x = 7 - x;
                }
                if (flipY) {
                    y = height() - 1 - y;
                }

                if (this.x + i >= Screen.WIDTH || this.x + i < 0) {
                    continue;
                }

                if (!prioritizeSprite && scanlineBuffer[this.x + i] != Color.WHITE) {
                    continue;
                }

                Tile tile = tiles[this.tileNumber + (y / TILE_HEIGHT)];

                int colorData = tile.graphics[y % TILE_HEIGHT][x];
                if (colorData == 0) {
                    continue;
                }

                scanlineBuffer[this.x + i] = tile.colorAt(x, y, palette);
            }
        }

        private int height() {
            return largeSprites ? SPRITE_HEIGHT : (SPRITE_HEIGHT / 2);
        }

        @Override
        public String toString() {
            return "Sprite(" + spriteNum + ")";
        }
    }

    private class Palette {
        private int palette;

        private Palette(int palette) {
            this.palette = palette;
        }

        private Color color(int input) {
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
