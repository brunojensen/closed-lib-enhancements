package solutions.kilian.legacy.enhancement;

import java.util.List;

import org.sonatype.aether.artifact.Artifact;

import javassist.ClassPool;
import javassist.NotFoundException;
import solutions.kilian.legacy.entry.EnhanceableEntry;
import solutions.kilian.legacy.file.EnhanceableFile;

public class BuilderEnhancement implements Enhancement {
    @Override
    public Artifact enhance(final EnhanceableFile enhanceableFile) {
        try {
            final ClassPool pool = new ClassPool();
            pool.appendPathList(enhanceableFile.getName());

            final ClassPool classPool = new ClassPool();
            classPool.appendClassPath(enhanceableFile.getArtifact().getFile().getPath());
            final List<EnhanceableEntry> entries = enhanceableFile.getEntries();
            for (final EnhanceableEntry entry : entries) {
                System.out.println(entry.getSimpleName());
                System.out.println(entry.getName());
            }

        } catch (final NotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
