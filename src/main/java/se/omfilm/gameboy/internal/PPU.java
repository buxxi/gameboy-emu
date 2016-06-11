package se.omfilm.gameboy.internal;

import se.omfilm.gameboy.internal.memory.Memory;
import se.omfilm.gameboy.io.color.ColorPalette;
import se.omfilm.gameboy.io.screen.Screen;

import java.awt.*;
import java.util.BitSet;
import java.util.function.Function;
import java.util.stream.IntStream;

import static se.omfilm.gameboy.internal.MMU.MemoryType.OBJECT_ATTRIBUTE_MEMORY;

public class PPU {
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
    private final ColorPalette colorPalette;
    private final Memory videoRAM;
    private final Memory objectAttributeMemory;

    private Palette backgroundPalette = new Palette(0);
    private Palette objectPalette0 = new Palette(0);
    private Palette objectPalette1 = new Palette(0);

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
    private int windowX = 0;

    private int cycleCounter = 0;
    private int scanline = 0;
    private int compareWithScanline = 0;
    private BitSet backgroundMask = new BitSet(Screen.WIDTH);

    private boolean lcdDisplay = false;
    private boolean windowDisplay = false;
    private boolean largeSprites = false;
    private boolean spriteDisplay = false;
    private boolean backgroundDisplay = false;

    private boolean coincidence = false;
    private boolean oamInterrupt = false;
    private boolean vblankInterrupt = false;
    private boolean hblankInterrupt = false;

    public PPU(Screen screen, ColorPalette colorPalette) {
        this.colorPalette = colorPalette;
        this.screen = screen;
        this.videoRAM = new VideoRAM();
        this.objectAttributeMemory = new ObjectAttributeMemory();
    }

    public void step(int cycles, Interrupts interrupts) {
        if (!updateCurrentMode(interrupts)) {
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

    private boolean updateCurrentMode(Interrupts interrupts) {
        if (!lcdDisplay) {
            cycleCounter = GPUMode.VBLANK.minimumCycles;
            mode = GPUMode.VBLANK;
            scanline = 0;
            return false;
        }

        if (scanline >= Screen.HEIGHT) {
            updateCurrentMode(interrupts, GPUMode.VBLANK, vblankInterrupt);
        } else if (cycleCounter >= GPUMode.VBLANK.minimumCycles - GPUMode.OAM.minimumCycles) {
            updateCurrentMode(interrupts, GPUMode.OAM, oamInterrupt);
        } else if (cycleCounter >= GPUMode.VBLANK.minimumCycles - GPUMode.OAM.minimumCycles - GPUMode.VRAM.minimumCycles) {
            updateCurrentMode(interrupts, GPUMode.VRAM, false);
        } else {
            updateCurrentMode(interrupts, GPUMode.HBLANK, hblankInterrupt);
        }

        if (coincidence && scanline == compareWithScanline) {
            interrupts.request(Interrupts.Interrupt.LCD);
        }

        return true;
    }

    private void updateCurrentMode(Interrupts interrupts, GPUMode newMode, boolean requestInterrupt) {
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
        int y = scanline + windowY;
        int adjustedX = ((x + windowX - 7) + Screen.WIDTH) % Screen.WIDTH;
        Tile tile = tileAt(adjustedX, y, windowTileMap);
        Shade shade = tile.shadeAt(adjustedX, y, backgroundPalette);
        drawPixel(x, shade, colorPalette::background);
    }

    private void drawBackgroundPixel(int x) {
        int y = scanline + scrollY;
        int adjustedX = x + scrollX;
        Tile tile = tileAt(adjustedX, y, backgroundTileMap);
        Shade shade = tile.shadeAt(adjustedX, y, backgroundPalette);
        drawPixel(x, shade, colorPalette::background);
    }

    private void drawPixel(int x, Shade shade, Function<Shade, Color> tranform) {
        backgroundMask.set(x, shade != Shade.LIGHTEST);
        screen.setPixel(x, scanline - 1, tranform.apply(shade));
    }

    private void drawSprites() {
        for (int i = 0; i < sprites.length; i++) {
            sprites[i].render();
        }
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

    public void scrollY(int data) {
        this.scrollY = data;
    }

    public void scrollX(int data) {
        this.scrollX = data;
    }

    public int scrollX() {
        return scrollX;
    }

    public int scrollY() {
        return scrollY;
    }

    public void windowY(int data) {
        this.windowY = data;
    }

    public int windowY() {
        return windowY;
    }

    public void windowX(int data) {
        this.windowX = data;
    }

    public int windowX() {
        return windowX;
    }

    public void backgroundPalette(int backgroundPaletteData) {
        this.backgroundPalette = new Palette(backgroundPaletteData);
    }

    public int backgroundPalette() {
        return backgroundPalette.palette;
    }

    public void objectPalette0(int data) {
        this.objectPalette0 = new Palette(data);
    }

    public int objectPalette0() {
        return objectPalette0.palette;
    }

    public void objectPalette1(int data) {
        this.objectPalette1 = new Palette(data);
    }

    public int objectPalette1() {
        return objectPalette1.palette;
    }

    public void control(int data) {
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

    public int control() {
        return  (lcdDisplay ?                       0b1000_0000 : 0) |
                (windowTileMap == tileMap1 ?        0b0100_0000 : 0) |
                (windowDisplay ?                    0b0010_0000 : 0) |
                (tileOffset == 0 ?                  0b0001_0000 : 0) |
                (backgroundTileMap == tileMap1 ?    0b0000_1000 : 0) |
                (largeSprites ?                     0b0000_0100 : 0) |
                (spriteDisplay ?                    0b0000_0010 : 0) |
                (backgroundDisplay ?                0b0000_0001 : 0);

    }

    public void interruptEnables(int data) {
        coincidence =       (data & 0b0100_0000) != 0;
        oamInterrupt =      (data & 0b0010_0000) != 0;
        vblankInterrupt =   (data & 0b0001_0000) != 0;
        hblankInterrupt =   (data & 0b0000_1000) != 0;
    }

    public int status() {
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
        for (int i = 0; i < OBJECT_ATTRIBUTE_MEMORY.size(); i++) {
            objectAttributeMemory.writeByte(MMU.MemoryType.OBJECT_ATTRIBUTE_MEMORY.from + i, ram.readByte(offset + i));
        }
    }

    public Memory videoRAM() {
        return videoRAM;
    }

    public Memory objectAttributeMemory() {
        return objectAttributeMemory;
    }

    public void reset() {
        control(0x91);
        scrollX(0x00);
        scrollY(0x00);
        scanlineCompare(0x00);
        backgroundPalette(0xFC);
        objectPalette0(0xFF);
        objectPalette1(0xFF);
        windowX(0x00);
        windowY(0x00);
    }

    private class VideoRAM implements Memory {
        public int readByte(int address) {
            if (address >= TILE_MAP_ADDRESS_0) {
                return readTileMapByte(address);
            } else {
                return readTileByte(address);
            }
        }

        public void writeByte(int address, int data) {
            if (address >= TILE_MAP_ADDRESS_0) {
                writeTileMapByte(address, data);
            } else {
                writeTileByte(address, data);
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

        private void writeTileByte(int address, int value) {
            int virtualAddress = address - MMU.MemoryType.VIDEO_RAM.from;

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

        private int readTileByte(int address) {
            int virtualAddress = address - MMU.MemoryType.VIDEO_RAM.from;
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

    }

    private class ObjectAttributeMemory implements Memory {
        public int readByte(int address) {
            int virtualAddress = address - MMU.MemoryType.OBJECT_ATTRIBUTE_MEMORY.from;
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
                            (sprite.palette == objectPalette1 ?         0b0001_0000 : 0);
            }
            throw new IllegalArgumentException("Unreachable code");
        }

        public void writeByte(int address, int data) {
            int virtualAddress = address - MMU.MemoryType.OBJECT_ATTRIBUTE_MEMORY.from;

            int spriteNumber = virtualAddress / SPRITE_BYTE_SIZE;
            int type = virtualAddress % SPRITE_BYTE_SIZE;

            Sprite sprite = sprites[spriteNumber];
            switch (type) {
                case 0:
                    sprite.y = data - SPRITE_HEIGHT;
                    break;
                case 1:
                    sprite.x = data - SPRITE_WIDTH;
                    break;
                case 2:
                    sprite.tileNumber = data;
                    break;
                case 3:
                    sprite.prioritizeSprite =   (data & 0b1000_0000) == 0;
                    sprite.flipY =              (data & 0b0100_0000) != 0;
                    sprite.flipX =              (data & 0b0010_0000) != 0;
                    sprite.palette =            (data & 0b0001_0000) == 0 ? objectPalette0 : objectPalette1;
            }
        }
    }

    private class Tile {
        private final int tileNumber;

        private final int[][] graphics = new int[TILE_HEIGHT][TILE_WIDTH];

        private Tile(int tileNumber) {
            this.tileNumber = tileNumber;
        }

        private Shade shadeAt(int x, int y, Palette palette) {
            return palette.shade(graphics[y % TILE_HEIGHT][x % TILE_WIDTH]);
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
        private Palette palette = objectPalette0;

        private Sprite(int spriteNum) {
            this.spriteNum = spriteNum;
        }

        private boolean isOnScanline() {
            int y = this.y;
            return scanline >= y && scanline < (y + height());
        }

        private void render() {
            if (!isOnScanline()) {
                return;
            }

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

                if (!prioritizeSprite && backgroundMask.get(this.x + i)) {
                    continue;
                }

                Tile tile = tiles[this.tileNumber + (y / TILE_HEIGHT)];

                int colorData = tile.graphics[y % TILE_HEIGHT][x];
                if (colorData == 0) {
                    continue;
                }

                Shade shade = tile.shadeAt(x, y, palette);
                drawPixel(this.x + i, shade, this::shadeToColor);
            }
        }

        private Color shadeToColor(Shade shade) {
            return colorPalette.sprite(shade, spritePaletteIndex());
        }

        private int spritePaletteIndex() {
            return palette == objectPalette0 ? 0 : 1;
        }

        private int height() {
            return largeSprites ? SPRITE_HEIGHT : (SPRITE_HEIGHT / 2);
        }

        @Override
        public String toString() {
            return "Sprite(" + spriteNum + ")";
        }
    }

    public enum Shade {
        DARKEST,
        DARK,
        LIGHT,
        LIGHTEST
    }

    private class Palette {
        private int palette;

        private Palette(int palette) {
            this.palette = palette;
        }

        private Shade shade(int input) {
            int offset = input * 2;
            int mask = (0b0000_0011 << offset);
            int result = (palette & mask) >> offset;

            switch (result) {
                case 0:
                default:
                    return Shade.LIGHTEST;
                case 1:
                    return Shade.LIGHT;
                case 2:
                    return Shade.DARK;
                case 3:
                    return Shade.DARKEST;
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
