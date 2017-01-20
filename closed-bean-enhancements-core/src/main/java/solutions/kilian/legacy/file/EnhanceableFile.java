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


    public EnhanceableFile(final Artifact artifact) throws IOException {
        super(artifact.getFile());
        this.artifact = artifact;
    }

    public List<EnhanceableEntry> getEntries() {
        final Enumeration<JarEntry> entries = super.entries();
        final List<EnhanceableEntry> enhanceableEntries = new ArrayList<EnhanceableEntry>(0);
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            if (this.isClassFile(entry) && !this.isExcluded(entry)) {
                enhanceableEntries.add(new EnhanceableEntry(entry));
            }
        }
        return enhanceableEntries;
    }

    public boolean isClassFile(final JarEntry entry) {
        return entry.getName().endsWith(CLASS_SUFFIX);
    }


    private boolean isExcluded(final JarEntry entry) {
        return exclusions != null && exclusions.contains(entry.getName());
    }

    public Set<String> getExclusions() {
        return exclusions;
    }

    public void setExclusions(Set<String> exclusions) {
        this.exclusions = exclusions;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

}
