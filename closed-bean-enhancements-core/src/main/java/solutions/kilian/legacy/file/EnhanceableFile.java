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
    private Artifact artifact;
    public static final String CLASS_SUFFIX = ".class";

    public EnhanceableFile(final Artifact artifact, final Set<String> exclusions) throws IOException {
        super(artifact.getFile());
        this.exclusions = exclusions;
        this.artifact = artifact;
    }

    public List<EnhanceableEntry> getEntries() {
        final Enumeration<JarEntry> entries = super.entries();
        final List<EnhanceableEntry> enhanceableEntries = new ArrayList<EnhanceableEntry>(0);
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            final EnhanceableEntry enhanceableEntry = new EnhanceableEntry(entry);
            if (enhanceableEntry.isClassFile() && !this.isExcluded(enhanceableEntry)) {
                enhanceableEntries.add(enhanceableEntry);
            }
        }
        return enhanceableEntries;
    }

    private boolean isExcluded(final EnhanceableEntry entry) {
        return exclusions != null && exclusions.contains(entry.getName());
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

}
