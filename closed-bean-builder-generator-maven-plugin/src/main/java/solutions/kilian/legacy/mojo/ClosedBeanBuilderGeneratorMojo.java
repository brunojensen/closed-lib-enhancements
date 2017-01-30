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

import javassist.ClassPool;
import javassist.NotFoundException;
import solutions.kilian.legacy.enhancement.BuilderEnhancement;
import solutions.kilian.legacy.enhancement.Enhancement;
import solutions.kilian.legacy.file.EnhaceableFileReplacer;
import solutions.kilian.legacy.file.EnhanceableFile;
import solutions.kilian.legacy.parameter.ClosedArtifact;

@Mojo(name = "generate")
public class ClosedBeanBuilderGeneratorMojo extends AbstractEnhancementMojo {

    private static final String ERROR_CODE = "[ERROR]";

    @Parameter(alias = "closed-artifacts")
    private List<ClosedArtifact> closedArtifacts = new ArrayList<ClosedArtifact>();

    @Parameter
    private Set<String> exclusions = new HashSet<String>();

    @Parameter(defaultValue = "enhanced", alias = "artifact-suffix")
    private String artifactSuffix;

    private List<EnhanceableFile> enhanceableFiles = new ArrayList<EnhanceableFile>(0);

    private ClassPool classPool = new ClassPool(true);

    @Override
    public void execute() throws MojoExecutionException {
        info("Exclusions:", exclusions);

        for (final ClosedArtifact closedArtifact : closedArtifacts) {
            Artifact originalArtifact = resolveOriginalArtifact(closedArtifact);
            try {
                classPool.appendPathList(originalArtifact.getFile().getPath());
                if (!closedArtifact.isDependency()) {
                    enhanceableFiles.add(new EnhanceableFile(originalArtifact, exclusions));
                }
            } catch (final IOException exception) {
                throw new MojoExecutionException(ERROR_CODE, exception);
            } catch (NotFoundException exception) {
                throw new MojoExecutionException(ERROR_CODE, exception);
            }
        }

        final Enhancement enhancement = new BuilderEnhancement(getLog(), classPool);
        for (final EnhanceableFile enhanceableFile : enhanceableFiles) {
            enhancement.enhance(enhanceableFile);
        }

        for (final EnhanceableFile enhanceableFile : enhanceableFiles) {
            File enhancedFile = generateFileWithEntries(enhanceableFile.getOriginalArtifact(), enhanceableFile);
            Artifact generatedArtifact = generateEnhancedArtifact(enhanceableFile.getOriginalArtifact(), enhancedFile);
            publish(generatedArtifact);
            enhancedFile.delete();
        }
    }

    private Artifact resolveOriginalArtifact(final ClosedArtifact closedArtifact) throws MojoExecutionException {
        final ArtifactResult artifactResult = resolve(closedArtifact.artifact());
        Artifact originalArtifact = artifactResult.getArtifact();
        getLog().info("Enhancing artifact: " + originalArtifact.getArtifactId());
        return originalArtifact;
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
