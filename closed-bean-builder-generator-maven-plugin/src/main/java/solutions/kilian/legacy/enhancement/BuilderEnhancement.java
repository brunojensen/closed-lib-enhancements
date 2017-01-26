package solutions.kilian.legacy.enhancement;

import java.io.IOException;

import org.apache.maven.plugin.logging.Log;

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

    @Override
    public void enhance(final EnhanceableFile enhanceableFile) {
        try {
            final ClassPool classPool = new ClassPool(true);
            classPool.appendPathList(enhanceableFile.getName());

            log.info("Enhanceable entries:");
            for (final EnhanceableEntry enhanceableEntry : enhanceableFile.getEntries()) {
                log.info(enhanceableEntry.getCanonicalName());
                try {
                    final CtClass ctClass = classPool.get(enhanceableEntry.getCanonicalName());
                    byte[] bytecode = null;
                    if (!ctClass.isEnum() && !ctClass.isInterface() && !ctClass.isPrimitive()) {
                        classPool.importPackage(ctClass.getPackageName());
                        bytecode = transformClass(ctClass).toBytecode();
                    } else {
                        bytecode = ctClass.toBytecode();
                    }
                    enhanceableEntry.setByteCode(bytecode);
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

    private CtClass transformClass(CtClass toModify) throws NotFoundException, CannotCompileException, IOException {
        for (final CtMethod ctMethod : toModify.getMethods()) {
            if (ctMethod.getName().startsWith("set")) {
            }
        }

        final CtMethod m = CtNewMethod.make(generateConstructorMethod(toModify), toModify);
        toModify.addMethod(m);
        toModify.writeFile();
        return toModify;
    }

    private String generateConstructorMethod(CtClass toModify) {
        StringBuilder constructorBuilder = new StringBuilder();
        constructorBuilder.append("public static synchronized ").append(toModify.getSimpleName()).append(" create() {");
        constructorBuilder.append("return new ").append(toModify.getSimpleName()).append("();").append("}");
        String constructorMethod = constructorBuilder.toString();
        return constructorMethod;
    }
}
