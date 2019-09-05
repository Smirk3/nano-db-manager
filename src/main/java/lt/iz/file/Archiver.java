package lt.iz.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Archiver {

    public static void compress(File sourceFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(sourceFile.toString() + ".zip");
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        zipFile(sourceFile, sourceFile.getName(), zipOut);
        zipOut.close();
        fos.close();
    }

    public static void extract(File sourceFile, File destDir) throws IOException {
        try (ZipFile file = new ZipFile(sourceFile)) {
            FileSystem fileSystem = FileSystems.getDefault();
            Enumeration<? extends ZipEntry> entries = file.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.isDirectory()) {
                    //System.out.println("Creating Directory:" + destDir + File.separator + entry.getName());
                    Files.createDirectories(fileSystem.getPath(destDir + File.separator + entry.getName()));

                } else {
                    try (InputStream is = file.getInputStream(entry);
                         BufferedInputStream bis = new BufferedInputStream(is)) {
                        String uncompressedFileName = destDir + File.separator + entry.getName();
                        Path uncompressedFilePath = fileSystem.getPath(uncompressedFileName);
                        Files.createFile(uncompressedFilePath);
                        FileOutputStream fileOutput = new FileOutputStream(uncompressedFileName);

                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = bis.read(bytes)) >= 0) {
                            fileOutput.write(bytes, 0, length);
                        }

                        fileOutput.close();
                        //System.out.println("Written :" + entry.getName());
                    }
                }
            }
        }
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

}
