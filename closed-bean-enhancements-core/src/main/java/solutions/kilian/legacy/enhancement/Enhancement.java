package solutions.kilian.legacy.enhancement;

import org.sonatype.aether.artifact.Artifact;

import solutions.kilian.legacy.file.EnhanceableFile;

public interface Enhancement {
    public Artifact enhance(final EnhanceableFile enhanceableFile);
}

