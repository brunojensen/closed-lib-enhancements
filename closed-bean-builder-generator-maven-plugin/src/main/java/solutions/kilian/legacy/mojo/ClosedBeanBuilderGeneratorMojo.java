package solutions.kilian.legacy.mojo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import solutions.kilian.legacy.enhancement.BuilderEnhancement;
import solutions.kilian.legacy.enhancement.Enhancement;
import solutions.kilian.legacy.file.EnhaceableFileReplacer;
import solutions.kilian.legacy.file.EnhanceableFile;
import solutions.kilian.legacy.parameter.ClosedArtifact;

@Mojo(name = "generate")
public class ClosedBeanBuilderGeneratorMojo extends AbstractEnhancementMojo {

    @Parameter(alias = "closed-artifacts")
    private List<ClosedArtifact> closedArtifacts = new ArrayList<ClosedArtifact>();

    @Parameter
    private Set<String> exclusions = new HashSet<String>();

    @Parameter(defaultValue = "enhanced", alias = "artifact-suffix")
    private String artifactSuffix;

    @Override
    public void execute() throws MojoExecutionException {
        for (final ClosedArtifact closedArtifact : closedArtifacts) {
            final ArtifactResult artifactResult = resolve(closedArtifact.artifact());
            Artifact originalArtifact = artifactResult.getArtifact();
            getLog().info("Enhancing artifact: " + originalArtifact.getArtifactId());

            EnhanceableFile enhanceableFile = null;
            try {
                enhanceableFile = new EnhanceableFile(originalArtifact.getFile(), exclusions);
                info("Exclusions:", exclusions);
            } catch (final IOException ioException) {
                getLog().error(ioException);
            }

            Enhancement enhancement = new BuilderEnhancement(getLog());
            enhancement.enhance(enhanceableFile);

            Artifact generatedArtifact = generateEnhancedArtifact(originalArtifact,
                    generateFileWithEntries(originalArtifact, enhanceableFile));
            publish(generatedArtifact);
        }
    }

    private File generateFileWithEntries(Artifact originalArtifact, EnhanceableFile enhanceableFile)
            throws MojoExecutionException {
        final EnhaceableFileReplacer replacer = new EnhaceableFileReplacer(
                originalArtifact.getArtifactId() + "-" + artifactSuffix);
        final File file = replacer.replace(originalArtifact.getFile(), enhanceableFile.getEntries());
        return file;
    }

    private Artifact generateEnhancedArtifact(final Artifact originalArtifact, final File file)
            throws MojoExecutionException {
        DefaultArtifact defaultArtifact = new DefaultArtifact(originalArtifact.getGroupId(),
                originalArtifact.getArtifactId() + "-" + artifactSuffix, originalArtifact.getExtension(),
                originalArtifact.getVersion());
        Artifact artifact = defaultArtifact.setFile(file);
        return artifact;
    }

}
