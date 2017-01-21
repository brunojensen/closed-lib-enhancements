package solutions.kilian.legacy.enhancement;

import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import solutions.kilian.legacy.entry.EnhanceableEntry;
import solutions.kilian.legacy.file.EnhanceableFile;
import solutions.kilian.legacy.file.JarHandler;

public class BuilderEnhancement implements Enhancement {

    private Log log;

    public BuilderEnhancement(final Log log) {
        this.log = log;
    }

    @Override
    public Artifact enhance(final EnhanceableFile enhanceableFile) {

        final DefaultArtifact artifact = new DefaultArtifact(enhanceableFile.getArtifact().getGroupId(),
                enhanceableFile.getArtifact().getArtifactId(), enhanceableFile.getArtifact().getExtension(),
                enhanceableFile.getArtifact().getVersion());
        artifact.setFile(enhanceableFile.getArtifact().getFile());

        try {
            final ClassPool pool = new ClassPool();
            pool.appendPathList(enhanceableFile.getName());

            final ClassPool classPool = new ClassPool();
            classPool.appendClassPath(enhanceableFile.getArtifact().getFile().getPath());
            final List<EnhanceableEntry> entries = enhanceableFile.getEntries();
            log.info("Enhanceable entries:");
            for (final EnhanceableEntry entry : entries) {
                log.info(entry.getSimpleName());
                final CtClass ctClass = pool.get(entry.getClearedName());
                try {

                    entry.setByteCode(ctClass.toBytecode());
                    final CtMethod m = CtNewMethod.make("public void xmove() {  }", ctClass);
                    ctClass.addMethod(m);
                    ctClass.writeFile();

                } catch (final CannotCompileException e) {
                    e.printStackTrace();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

            final JarHandler jarHandler = new JarHandler();
            jarHandler.replaceEntriesInJarFile(enhanceableFile);
            log.info("Enhanced file at: " + enhanceableFile.getArtifact().getFile().getPath());

        } catch (final NotFoundException e) {
            log.error(e);
        }

        return null;
    }
}
