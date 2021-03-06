package solutions.kilian.legacy.mojo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

public abstract class AbstractEnhancementMojo extends AbstractMojo {

    @Component
    private RepositorySystem repositorySystem;

    @Parameter(defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession repositorySystemSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}")
    private List<RemoteRepository> remoteRepositories;

    protected void info(final String description, final Set<String> set) {
        getLog().info(description);
        final Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            getLog().info(iterator.next());
        }
    }

    protected ArtifactResult resolve(final DefaultArtifact defaultArtifact) throws MojoExecutionException {
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

    protected void publish(Artifact artifact) {
        InstallRequest installRequest = new InstallRequest();
        installRequest.setArtifacts(new ArrayList<Artifact>(0));
        installRequest.getArtifacts().add(artifact);
        try {
            repositorySystem.install(repositorySystemSession, installRequest);
        } catch (InstallationException e) {
            getLog().error(e.getMessage());
        }
    }
}
