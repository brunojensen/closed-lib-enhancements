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
    private final Artifact originalArtifact;

    public EnhanceableFile(final Artifact originalArtifact, final Set<String> exclusions) throws IOException {
        super(originalArtifact.getFile());
        this.originalArtifact = originalArtifact;
        this.exclusions = exclusions;
    }

    public List<EnhanceableEntry> getEntries() {
        if (enhanceableEntries == null) {
            this.enhanceableEntries = new ArrayList<EnhanceableEntry>(0);
            final Enumeration<JarEntry> entries = super.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final EnhanceableEntry enhanceableEntry = new EnhanceableEntry(entry);
                if (enhanceableEntry.isClassFile() && !this.isExcluded(enhanceableEntry)
                        && !this.isPackageEntry(enhanceableEntry)) {
                    enhanceableEntries.add(enhanceableEntry);
                }
            }
        }
        return enhanceableEntries;
    }

    private boolean isPackageEntry(final EnhanceableEntry entry) {
        return entry.getCanonicalName().endsWith("ObjectFactory") || entry.getCanonicalName().endsWith("package-info");
    }

    private boolean isExcluded(final EnhanceableEntry entry) {
        return exclusions != null && exclusions.contains(entry.getCanonicalName());
    }

    public Artifact getOriginalArtifact() {
        return originalArtifact;
    }

}
