package solutions.kilian.legacy.enhancement;

import java.io.IOException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import org.apache.maven.plugin.logging.Log;

import solutions.kilian.legacy.entry.EnhanceableEntry;
import solutions.kilian.legacy.file.EnhanceableFile;

public class BuilderEnhancement implements Enhancement {

    private Log log;

    public BuilderEnhancement(final Log log) {
        this.log = log;
    }

    @Override
    public void enhance(final EnhanceableFile enhanceableFile) {
        try {
            final ClassPool classPool = new ClassPool();
            classPool.appendPathList(enhanceableFile.getName());

            log.info("Enhanceable entries:");
            for (final EnhanceableEntry enhanceableEntry : enhanceableFile.getEntries()) {
                log.info(enhanceableEntry.getCanonicalName());
                try {
                    /*
                     * TODO: Desenvolver algum pattern para executar isso. Talvez mais um strategy ou decorator
                     */
                    transformClass(classPool, enhanceableEntry);
                } catch (final CannotCompileException e) {
                    log.error(e.getMessage());
                } catch (final IOException e) {
                    log.error(e.getMessage());
                }
            }
        } catch (final NotFoundException e) {
            log.error(e.getMessage());
        }
    }

    private void transformClass(ClassPool pool, EnhanceableEntry enhanceableEntry) throws NotFoundException,
            CannotCompileException, IOException {
        final CtClass ctClass = pool.get(enhanceableEntry.getCanonicalName());
        final CtMethod m = CtNewMethod.make("public void xmove() {  }", ctClass);
        ctClass.addMethod(m);
        ctClass.writeFile();
        enhanceableEntry.setByteCode(ctClass.toBytecode());
    }
}
