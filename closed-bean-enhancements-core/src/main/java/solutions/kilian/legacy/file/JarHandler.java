package solutions.kilian.legacy.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class JarHandler {
    public void replaceJarFile(String jarPathAndName, byte[] fileByteCode, String fileName) throws IOException {
        JarFile jarFile = new JarFile(new File(jarPathAndName));
        File tempFile = new File(jarPathAndName + "-enhancements");
        try {
            JarOutputStream tempJarFile = new JarOutputStream(new FileOutputStream(tempFile));
            byte[] buffer = new byte[1024];
            int bytesRead;

            try {
                try {
                    JarEntry entry = new JarEntry(fileName);
                    tempJarFile.putNextEntry(entry);
                    tempJarFile.write(fileByteCode);
                } catch (Exception ex) {
                    System.out.println(ex);
                    tempJarFile.putNextEntry(new JarEntry("stub"));
                }

                InputStream entryStream = null;
                for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                    JarEntry entry = (JarEntry) entries.nextElement();
                    if (!entry.getName().equals(fileName)) {
                        entryStream = jarFile.getInputStream(entry);
                        tempJarFile.putNextEntry(entry);
                        while ((bytesRead = entryStream.read(buffer)) != -1) {
                            tempJarFile.write(buffer, 0, bytesRead);
                        }
                    }
                }
                if (entryStream != null) {
                    entryStream.close();
                }

            } catch (Exception ex) {
                System.out.println(ex);
                tempJarFile.putNextEntry(new JarEntry("stub"));
            } finally {
                tempJarFile.close();
            }
        } finally {
            jarFile.close();
        }
    }
}