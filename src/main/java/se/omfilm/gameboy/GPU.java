package se.omfilm.gameboy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GPU implements Memory {
    private final Memory videoRam;
    private final IOController ioController;

    public GPU(Memory videoRam, IOController ioController) {
        this.videoRam = videoRam;
        this.ioController = ioController;
    }

    public void step(int cycles) {

    }

    public int readByte(int address) {
        return videoRam.readByte(address);
    }

    public void writeByte(int address, int data) {
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
}
