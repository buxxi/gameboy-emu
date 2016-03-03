package se.omfilm.gameboy.io;

import se.omfilm.gameboy.ByteArrayMemory;
import se.omfilm.gameboy.Memory;
import se.omfilm.gameboy.io.screen.Screen;

import java.awt.*;

public class GPU implements Memory {
    private final Memory videoRam;
    private final Screen screen;

    private GPUMode mode = GPUMode.HBLANK;
    private int scrollX = 0;
    private int scrollY = 0;
    private int paletteData;
    private int cycleCounter = 0;
    private int scanline = 0;
    private int lcdControl = 0;

    public GPU(Screen screen) {
        this.videoRam = new ByteArrayMemory(Memory.MemoryType.VIDEO_RAM.allocate());
        this.screen = screen;
    }

    public void step(int cycles) {
        if (!isLcdOn()) {
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
        if (true) { //TODO: check bit on lcd status
            drawTiles(screen);
        }
    }

    private void drawTiles(Screen screen) {
        for (int i = 0; i < Screen.WIDTH; i++) {
            int y = scanline + scrollY;
            int x = i + scrollX;
            int tileNumber = resolveTileNumber(y, x);
            int rowData = resolveRowData(tileNumber, y);
            Color color = color(colorData(rowData, x % 8)); //TODO: handle palette to get the correct color
            screen.setPixel(x, scanline - 1, color);
        }
    }

    private int resolveRowData(int tileNumber, int y) {
        int tileDataAddressFrom = 0x8000 - MemoryType.VIDEO_RAM.from; //TODO: check bit on lcd status, this is now unsigned
        int tileLocation = tileDataAddressFrom + (tileNumber * 8 * 2); //Since each tile is 2 bytes and 8 rows long
        return videoRam.readWord(tileLocation + ((y % 8) * 2));
    }

    private int resolveTileNumber(int y, int x) {
        int tileMapAddressFrom = 0x9800 - MemoryType.VIDEO_RAM.from; //TODO: check bit on lcd status
        int address = tileMapAddressFrom + ((y / 8) * 32) + (x / 8); //Each row contains 32 sprites
        return videoRam.readByte(address);
    }

    public int readByte(int address) {
        if (!mode.accessVideoRAM) {
            throw new IllegalStateException("The CPU can't access VideoRAM while in mode " + mode);
        }
        return videoRam.readByte(address);
    }

    public void writeByte(int address, int data) {
        if (!mode.accessVideoRAM) {
            throw new IllegalStateException("The CPU can't access VideoRAM while in mode " + mode);
        }
        videoRam.writeByte(address, data);
    }

    public void scrollY(int data) {
        this.scrollY = data;
    }

    public int scrollY() {
        return scrollY;
    }

    public void setPaletteData(int paletteData) {
        this.paletteData = paletteData;
    }

    public void setLCDControl(int data) {
        if (data != 0x91) {
            throw new IllegalArgumentException("Can only handle 0x91 as lcd control value for now");
        }
        this.lcdControl = data;
        screen.turnOn();
    }

    public int scanline() {
        return scanline;
    }

    private boolean isLcdOn() {
        return lcdControl == 0x91; //TODO: fix magic value by checking bits individually
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
