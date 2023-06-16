package digital.slovensko.autogram.core;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipProcessor {

    public static ExtractedFile processZip(String zipFilePath) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                if (!entry.isDirectory() && !isMetaData(entry) && !isMimeType(entry)) {
                    byte[] fileData = processZipEntry(zipInputStream);
                    return new ExtractedFile(entry.getName(), fileData);
                }
                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }
        }
        return null;
    }

    private static boolean isMimeType(ZipEntry entry) {
        return "mimetype".equals(entry.getName());
    }

    private static boolean isMetaData(ZipEntry entry) {
        String parentDirectory = getParentDirectory(entry.getName());
        return "META-INF".equals(parentDirectory);
    }

    private static String getParentDirectory(String filePath) {
        int lastSeparatorIndex = filePath.lastIndexOf("/");
        if (lastSeparatorIndex == -1) {
            lastSeparatorIndex = filePath.lastIndexOf("\\");
        }
        return (lastSeparatorIndex != -1) ? filePath.substring(0, lastSeparatorIndex) : "";
    }

    private static byte[] processZipEntry(ZipInputStream zipInputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        return outputStream.toByteArray();
    }
}
