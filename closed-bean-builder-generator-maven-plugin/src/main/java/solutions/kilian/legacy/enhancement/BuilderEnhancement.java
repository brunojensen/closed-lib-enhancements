package solutions.kilian.legacy.enhancement;

import java.io.IOException;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import org.apache.maven.plugin.logging.Log;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

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
            String filePath = enhanceableFile.getArtifact().getFile().getPath();
            classPool.appendClassPath(filePath);
            final List<EnhanceableEntry> entries = enhanceableFile.getEntries();
            log.info("Enhanceable entries:");
            final JarHandler jarHandler = new JarHandler();
            for (final EnhanceableEntry entry : entries) {
                log.info(entry.getSimpleName());
                CtClass point = pool.get(entry.getName());
                try {
                    CtMethod m = CtNewMethod.make("public void xmove() {  }", point);
                    point.addMethod(m);
                    point.writeFile();
                    jarHandler.replaceJarFile(filePath, point.toBytecode(), entry.getJarName());
                } catch (CannotCompileException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            log.info("Enhanced file at: " + filePath);

        } catch (final NotFoundException e) {
            log.error(e);
        }

        return null;
    }
}
