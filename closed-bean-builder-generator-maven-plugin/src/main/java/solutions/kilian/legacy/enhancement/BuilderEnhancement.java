package solutions.kilian.legacy.enhancement;

import java.io.IOException;

import org.apache.maven.plugin.logging.Log;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import solutions.kilian.legacy.entry.EnhanceableEntry;
import solutions.kilian.legacy.file.EnhanceableFile;

public class BuilderEnhancement implements Enhancement {

    private Log log;

    public BuilderEnhancement(final Log log) {
        this.log = log;
    }


    /*
     * TODO: Se a entrada é um enhanceable file e a saida é um enhanceable file talvez isso aqui
     * vire um decorator com strategy dentro dele
     */
    @Override
    public void enhance(final EnhanceableFile enhanceableFile) {


        /*
         * TODO: se tudo que tenho que fazer é devolver uma artifact que é instanciado pelo
         * enhanceablFile, talvez nao seja necessário devolve-lo. Construir uma implementação do
         * enhanceableFile com o os maven coords modificados para não ter que devolver um artifact
         * daqui
         */
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
                log.info(enhanceableEntry.getCanonicalName());
                try {
                    /*
                     * TODO: Desenvolver algum pattern para executar isso. Talvez mais um strategy
                     * ou decorator
                     */
                    transformClass(pool, enhanceableEntry);
                } catch (final CannotCompileException e) {
                    e.printStackTrace();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (final NotFoundException e) {
            log.error(e);
        }

    }



    private void transformClass(ClassPool pool, EnhanceableEntry enhanceableEntry)
            throws NotFoundException, CannotCompileException, IOException {
        final CtClass ctClass = pool.get(enhanceableEntry.getCanonicalName());
        final CtMethod m = CtNewMethod.make("public void xmove() {  }", ctClass);
        ctClass.addMethod(m);
        ctClass.writeFile();
        enhanceableEntry.setByteCode(ctClass.toBytecode());
    }
}
