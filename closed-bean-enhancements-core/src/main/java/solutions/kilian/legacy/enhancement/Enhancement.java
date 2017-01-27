package solutions.kilian.legacy.enhancement;

import org.apache.maven.plugin.MojoExecutionException;

import solutions.kilian.legacy.file.EnhanceableFile;

public interface Enhancement {
    public void enhance(final EnhanceableFile enhanceableFile) throws MojoExecutionException;
}
