package solutions.kilian.legacy.enhancement;

import java.io.IOException;

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

            log.info("Enhanceable entries:");
            for (final EnhanceableEntry enhanceableEntry : enhanceableFile.getEntries()) {
                log.info(enhanceableEntry.getSimpleName());
                try {
                    transformClass(pool, enhanceableEntry);
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

    private void transformClass(final ClassPool pool, final EnhanceableEntry enhanceableEntry)
            throws NotFoundException, CannotCompileException, IOException {
        final CtClass ctClass = pool.get(enhanceableEntry.getCanonicalName());
        final CtMethod m = CtNewMethod.make("public void xmove() {  }", ctClass);
        ctClass.addMethod(m);
        ctClass.writeFile();
        ctClass.defrost();
        enhanceableEntry.setByteCode(ctClass.toBytecode());
    }
}
