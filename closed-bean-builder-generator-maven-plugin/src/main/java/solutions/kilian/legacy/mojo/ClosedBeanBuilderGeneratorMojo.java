package solutions.kilian.legacy.mojo;

import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import solutions.kilian.legacy.enhancement.BuilderEnhancement;
import solutions.kilian.legacy.file.EnhanceableFile;
import solutions.kilian.legacy.parameter.ClosedArtifact;

@Mojo(name = "generate")
public class ClosedBeanBuilderGeneratorMojo extends AbstractMojo {

    @Parameter(property = "project", defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Component
    private RepositorySystem repositorySystem;

    @Parameter(defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession repositorySystemSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}")
    private List<RemoteRepository> remoteRepositories;

    @Parameter(alias = "closed-artifacts")
    private List<ClosedArtifact> closedArtifacts;



    /*
     * TODO: é necessário determinar como vai ser o uso dos vários comportamentos de parse nas
     * classes
     */
    @Override
    public void execute() throws MojoExecutionException {
        for (final ClosedArtifact closedArtifact : closedArtifacts) {
            final ArtifactResult artifactResult = resolveArtifact(closedArtifact.artifact());
            EnhanceableFile enhanceableFile = null;
            try {
                enhanceableFile = new EnhanceableFile(artifactResult.getArtifact());
            } catch (final IOException e) {
                e.printStackTrace();
            }

            final Artifact enhancedArtifact = new BuilderEnhancement().enhance(enhanceableFile);
            // usar artefato alterado acima para distribuir no repo local
            publishArtifact(enhancedArtifact);
        }
    }



    /* TODO: mover essa resolução de artefatos para outra classe talvez */
    private ArtifactResult resolveArtifact(final DefaultArtifact defaultArtifact) throws MojoExecutionException {
        final ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(defaultArtifact);
        artifactRequest.setRepositories(remoteRepositories);
        getLog().info("Resolving artifact " + artifactRequest.getArtifact() + " from " + remoteRepositories);

        try {
            final ArtifactResult result = repositorySystem.resolveArtifact(repositorySystemSession, artifactRequest);
            getLog().info("Resolved artifact " + result.getArtifact() + " to " + result.getArtifact().getFile()
                    + " from " + result.getRepository());
            return result;
        } catch (final ArtifactResolutionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void publishArtifact(Artifact enhancedArtifact) {

    }
}
