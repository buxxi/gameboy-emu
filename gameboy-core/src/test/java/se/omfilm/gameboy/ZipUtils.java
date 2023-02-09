package se.omfilm.gameboy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {
    public static byte[] readClassPathZipFile(String zipName, String fileName) throws IOException {
        try (ZipInputStream zipFile = new ZipInputStream(Objects.requireNonNull(ZipUtils.class.getClassLoader().getResourceAsStream(zipName)))) {
            ZipEntry entry;
            do {
                entry = zipFile.getNextEntry();
                if (entry != null && fileName.equals(entry.getName())) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    zipFile.transferTo(out);
                    return out.toByteArray();
                }
            } while (entry != null);
            throw new IllegalArgumentException(fileName + " not found in " + zipName);
        }
    }
}
