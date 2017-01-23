package solutions.kilian.legacy.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.sonatype.aether.artifact.Artifact;

import solutions.kilian.legacy.entry.EnhanceableEntry;

public class EnhanceableFile extends JarFile {
    private Set<String> exclusions;
    public static final String CLASS_SUFFIX = ".class";
    private List<EnhanceableEntry> enhanceableEntries;

    public EnhanceableFile(final Artifact artifact, final Set<String> exclusions) throws IOException {
        super(artifact.getFile());
        this.exclusions = exclusions;
    }

    public List<EnhanceableEntry> getEntries() {
        if (enhanceableEntries == null) {
            this.enhanceableEntries = new ArrayList<EnhanceableEntry>(0);
            final Enumeration<JarEntry> entries = super.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final EnhanceableEntry enhanceableEntry = new EnhanceableEntry(entry);
                if (enhanceableEntry.isClassFile() && !this.isExcluded(enhanceableEntry)) {
                    enhanceableEntries.add(enhanceableEntry);
                }
            }
        }
        return enhanceableEntries;
    }

    private boolean isExcluded(final EnhanceableEntry entry) {
        return exclusions != null && exclusions.contains(entry.getCanonicalName());
    }

}
