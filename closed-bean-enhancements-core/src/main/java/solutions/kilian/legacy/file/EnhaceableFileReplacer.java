package solutions.kilian.legacy.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.apache.maven.plugin.MojoExecutionException;

import solutions.kilian.legacy.entry.EnhanceableEntry;

public class EnhaceableFileReplacer {
    private static final byte[] READER_BUFFER = new byte[1024];
    private final String enhancedFileName;

    public EnhaceableFileReplacer(final EnhanceableFile enhanceableFile) {
        this.enhancedFileName = enhanceableFile.getName();
    }

    public EnhaceableFileReplacer(String enhancedFileName) {
        this.enhancedFileName = enhancedFileName;
    }

    public File replace(final File originalFile, final List<EnhanceableEntry> enhanceableEntries)
            throws MojoExecutionException {
        JarFile originalJarFile = null;
        File createdFile = null;
        try {
            originalJarFile = new JarFile(originalFile);

            String updatedJarPath = originalFile.getPath().substring(0,
                    originalFile.getPath().indexOf(originalFile.getName()));
            createdFile = new File(updatedJarPath + enhancedFileName);
            final JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(createdFile));
            try {
                writeModifiedEntries(enhanceableEntries, jarOutputStream);
                writeRemainingEntries(enhanceableEntries, originalJarFile, jarOutputStream);
            } catch (final Exception exception) {
                throw new MojoExecutionException(exception.getMessage(), exception.getCause());
            } finally {
                jarOutputStream.close();
            }
        } catch (final FileNotFoundException exception) {
            throw new MojoExecutionException(exception.getMessage(), exception.getCause());
        } catch (final IOException exception) {
            throw new MojoExecutionException(exception.getMessage(), exception.getCause());
        } finally {
            try {
                originalJarFile.close();
            } catch (final IOException exception) {
                throw new MojoExecutionException(exception.getMessage(), exception.getCause());
            }
        }

        return createdFile;
    }

    private void writeRemainingEntries(final List<EnhanceableEntry> enhanceableEntries, JarFile originalJarFile,
            final JarOutputStream replacedJarFile) throws IOException {
        final Enumeration<JarEntry> entries = originalJarFile.entries();
        InputStream inputSream = null;
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();

            if (!enhanceableEntries.contains(new EnhanceableEntry(entry))) {
                inputSream = originalJarFile.getInputStream(entry);
                replacedJarFile.putNextEntry(entry);
                int bytesRead = 0;
                while ((bytesRead = inputSream.read(READER_BUFFER)) != -1) {
                    replacedJarFile.write(READER_BUFFER, 0, bytesRead);
                }
            }
        }

        if (inputSream != null) {
            inputSream.close();
        }
    }

    private void writeModifiedEntries(final List<EnhanceableEntry> enhanceableEntries,
            final JarOutputStream newJarOutputStream) throws MojoExecutionException {
        for (final EnhanceableEntry entry : enhanceableEntries) {
            try {
                final JarEntry jarEntry = new JarEntry(entry.getName());
                newJarOutputStream.putNextEntry(jarEntry);
                newJarOutputStream.write(entry.getByteCode());
            } catch (final Exception exception) {
                throw new MojoExecutionException(exception.getMessage(), exception.getCause());
            }
        }
    }
}
