package solutions.kilian.legacy.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import solutions.kilian.legacy.entry.EnhanceableEntry;


/*
 * TODO: Extrair jar file para fora
 * Refatorar enhanceable entry
 * Refatorar Enhanceable file
 * Adicionar o comportamento em file?
 */
public class JarHandler {
    public void replaceEntriesInJarFile(final EnhanceableFile enhanceableFile) {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(new File(enhanceableFile.getArtifact().getFile().getPath()));
            final JarOutputStream replacedJarFile = new JarOutputStream(new FileOutputStream(
                    new File(enhanceableFile.getArtifact().getFile().getPath() + "-enhancements")));
            int bytesRead;

            try {
                try {

                    /* TODO: criar um toEntry */
                    for (final EnhanceableEntry entry : enhanceableFile.getEntries()) {
                        final JarEntry jarEntry = new JarEntry(entry.getJarName());
                        replacedJarFile.putNextEntry(jarEntry);
                        replacedJarFile.write(entry.getByteCode());
                    }

                } catch (final Exception ex) {
                    System.out.println(ex);
                    replacedJarFile.putNextEntry(new JarEntry("stub"));
                }

                InputStream entryStream = null;
                final byte[] buffer = new byte[1024];
                for (final Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                    final JarEntry entry = entries.nextElement();
                    if (!enhanceableFile.getEntries().contains(entry.getName())) {
                        entryStream = jarFile.getInputStream(entry);
                        replacedJarFile.putNextEntry(entry);
                        while ((bytesRead = entryStream.read(buffer)) != -1) {
                            replacedJarFile.write(buffer, 0, bytesRead);
                        }
                    }
                }
                if (entryStream != null) {
                    entryStream.close();
                }

            } catch (final Exception ex) {
                System.out.println(ex);
                replacedJarFile.putNextEntry(new JarEntry("stub"));
            } finally {
                replacedJarFile.close();
            }
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            try {
                jarFile.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
}
