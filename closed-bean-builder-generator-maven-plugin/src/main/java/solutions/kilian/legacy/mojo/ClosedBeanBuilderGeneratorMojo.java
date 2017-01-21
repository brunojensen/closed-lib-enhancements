package solutions.kilian.legacy.mojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.sonatype.aether.resolution.ArtifactResult;

import solutions.kilian.legacy.enhancement.BuilderEnhancement;
import solutions.kilian.legacy.file.EnhanceableFile;
import solutions.kilian.legacy.parameter.ClosedArtifact;

@Mojo(name = "generate")
public class ClosedBeanBuilderGeneratorMojo extends AbstractEnhancementMojo {

    @Parameter(alias = "closed-artifacts")
    private List<ClosedArtifact> closedArtifacts = new ArrayList<ClosedArtifact>();

    @Parameter
    private Set<String> exclusions = new HashSet<String>();

    @Override
    public void execute() throws MojoExecutionException {
        for (final ClosedArtifact closedArtifact : closedArtifacts) {
            final ArtifactResult artifactResult = resolveArtifact(closedArtifact.artifact());
            EnhanceableFile enhanceableFile = null;
            try {
                enhanceableFile = new EnhanceableFile(artifactResult.getArtifact(), exclusions);
                info("Exclusions:", exclusions);
            } catch (final IOException ioException) {
                getLog().error(ioException);
            }

            getLog().info("Enhancing artifact: " + enhanceableFile.getArtifact().getArtifactId());
            publishArtifact(new BuilderEnhancement(getLog()).enhance(enhanceableFile));
        }
    }

}
