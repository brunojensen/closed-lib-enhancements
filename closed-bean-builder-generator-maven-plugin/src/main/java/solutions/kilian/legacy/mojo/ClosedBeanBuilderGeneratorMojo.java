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
import solutions.kilian.legacy.file.EnhanceableFile;
import solutions.kilian.legacy.file.JarWriter;
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
            final ArtifactResult artifactResult = resolve(closedArtifact.artifact());
            Artifact originalArtifact = artifactResult.getArtifact();
            getLog().info("Enhancing artifact: " + originalArtifact.getArtifactId());

            EnhanceableFile enhanceableFile = null;
            try {
                enhanceableFile = new EnhanceableFile(originalArtifact, exclusions);
                info("Exclusions:", exclusions);
            } catch (final IOException ioException) {
                getLog().error(ioException);
            }

            Enhancement enhancement = new BuilderEnhancement(getLog());
            enhancement.enhance(enhanceableFile);

            Artifact generatedArtifact = generateEnhancedArtifact(originalArtifact, enhanceableFile);
            publish(generatedArtifact);
        }
    }

    /*
     * TODO: refatorar. Não é responsabilidade desta classe.
     */
    private Artifact generateEnhancedArtifact(final Artifact originalArtifact, final EnhanceableFile enhanceableFile) {
        final JarWriter jarHandler = new JarWriter(getLog(), originalArtifact.getArtifactId() + "-enhanced.jar");
        File file = jarHandler.generateJarFileWithEntries(originalArtifact.getFile(), enhanceableFile.getEntries());
        getLog().info("Enhanced file at: " + file.getPath());

        DefaultArtifact defaultArtifact = new DefaultArtifact(originalArtifact.getGroupId(),
                originalArtifact.getArtifactId() + "-enhanced", originalArtifact.getExtension(),
                originalArtifact.getVersion());
        Artifact artifact = defaultArtifact.setFile(file);
        return artifact;
    }

}
