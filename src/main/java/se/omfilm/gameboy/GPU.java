package se.omfilm.gameboy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GPU implements Memory {
    private static final int SCANLINE_TOTAL_CYCLE_COUNT = 456;
    private static final int SCANLINE_OAM_CYCLE_COUNT = 80;
    private static final int SCANLINE_VRAM_CYCLE_COUNT = 172;
    private static final int HEIGHT = 144;

    private final Memory videoRam;

    private GPUMode mode = GPUMode.HBLANK;
    private int scrollY;
    private int paletteData;

    private int scanlineCounter = SCANLINE_TOTAL_CYCLE_COUNT;
    private int scanline = 0;
    private int lcdControl = 0;

    public GPU(Memory videoRam) {
        this.videoRam = videoRam;
    }

    public void step(int cycles) {
        if (!isLcdOn()) {
            return;
        }
        scanlineCounter -= cycles;

        if (scanlineCounter <= 0) {
            scanline = (scanline + 1) & 0xFF;
            scanlineCounter = SCANLINE_TOTAL_CYCLE_COUNT;

            if (scanline == HEIGHT) {
                mode = GPUMode.VBLANK;
                drawToScreen();
            } else if (scanline > (HEIGHT + 9)) {
                scanline = 0;
            } else {
                drawScanline();
            }
        } else if ((SCANLINE_TOTAL_CYCLE_COUNT - SCANLINE_OAM_CYCLE_COUNT + cycles) >= scanlineCounter) {
            mode = GPUMode.OAM;
        } else if ((SCANLINE_TOTAL_CYCLE_COUNT - SCANLINE_OAM_CYCLE_COUNT - SCANLINE_VRAM_CYCLE_COUNT + cycles) >= scanlineCounter) {
            mode = GPUMode.VRAM;
        } else {
            mode = GPUMode.HBLANK;
        }
    }

    private void drawToScreen() {

    }

    private void drawScanline() {

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

    public void dumpTiles() {
        int[][][] tiles = readTiles();

        int tilesPerRow = (int) Math.ceil(Math.sqrt(383));
        BufferedImage result = new BufferedImage(tilesPerRow * 8, tilesPerRow * 8, BufferedImage.TYPE_INT_RGB);
        for (int tile = 0; tile < tiles.length; tile++) {
            int yOffset = (tile / tilesPerRow) * 8;
            int xOffset = (tile % tilesPerRow) * 8;

            for (int y = 0; y < tiles[tile].length; y++) {
                for (int x = 0; x < tiles[tile][y].length; x++) {
                    switch (tiles[tile][y][x]) {
                        case 0:
                            result.setRGB(xOffset + x, yOffset + y, 0xFFFFFF);
                            break;
                        case 1:
                            result.setRGB(xOffset + x, yOffset + y, 0xAAAAAA);
                            break;
                        case 2:
                            result.setRGB(xOffset + x, yOffset + y, 0x555555);
                            break;
                        case 3:
                            result.setRGB(xOffset + x, yOffset + y, 0x000000);
                    }
                }
            }
        }
        try {
            ImageIO.write(result, "png", File.createTempFile("gameboy", ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        this.lcdControl = data;
    }

    public int scanline() {
        return scanline;
    }

    private boolean isLcdOn() {
        return lcdControl == 0x91; //TODO: fix magic value by checking bits individually
    }

    private int[][][] readTiles() {
        int[][][] tiles = new int[384][8][8];
        for (int i = 0x8000; i < 0x97FF; i = i + 2) {
            int tile = (i >> 4) & 0x1FF;
            int y = (i >> 1) & 7;

            int x, bitIndex;
            for (x = 0; x < 8; x++) {
                bitIndex = 1 << (7 - x);

                tiles[tile][y][x] = ((readByte(i - 0x8000) & bitIndex) != 0 ? 1 : 0) + ((readByte(i + 1 - 0x8000) & bitIndex) != 0 ? 2 : 0);
            }
        }
        return tiles;
    }

    private enum GPUMode {
        HBLANK(0b00, true, true),
        VBLANK(0b01, true, true),
        OAM(0b10, false, true),
        VRAM(0b11, false, false);

        private final int bitMask;
        private final boolean accessOAM;
        private final boolean accessVideoRAM;

        GPUMode(int bitMask, boolean accessOAM, boolean accessVideoRAM) {
            this.bitMask = bitMask;
            this.accessOAM = accessOAM;
            this.accessVideoRAM = accessVideoRAM;
        }
    }
}
