package solutions.kilian.legacy.enhancement;

import java.util.Iterator;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import solutions.kilian.legacy.entry.EnhanceableEntry;
import solutions.kilian.legacy.file.EnhanceableFile;

public class BuilderEnhancement extends AbstractEnhancement {

    private final Log log;
    private static final String ERROR_CODE = "[ERROR]";

    public BuilderEnhancement(final Log log, final ClassPool classPool) {
        super(classPool);
        this.log = log;
    }

    @Override
    public void enhance(final EnhanceableFile enhanceableFile) throws MojoExecutionException {
        try {
            for (final EnhanceableEntry enhanceableEntry : enhanceableFile.getEntries()) {
                log.info(enhanceableEntry.getCanonicalName());
                final CtClass ctClass = getClassPool().get(enhanceableEntry.getCanonicalName());
                byte[] bytecode = null;
                if (!ctClass.isEnum() && !ctClass.isInterface() && !ctClass.isPrimitive()) {
                    importPackage(getClassPool(), ctClass.getPackageName());
                    bytecode = transformClass(ctClass, getClassPool());
                } else {
                    bytecode = ctClass.toBytecode();
                }
                enhanceableEntry.setByteCode(bytecode);
            }
        } catch (final Exception exception) {
            throw new MojoExecutionException(ERROR_CODE, exception);
        }
    }

    private byte[] transformClass(CtClass ctClassToModify, ClassPool classPool) throws MojoExecutionException {
        for (final CtMethod ctMethod : ctClassToModify.getDeclaredMethods()) {
            if (ctMethod.getName().startsWith("set")) {
                final String entityName = ctMethod.getName().substring(3);
                CtMethod withMethod = null;
                try {
                    withMethod = generateMethodBodyWithBuilder(classPool, ctClassToModify, ctMethod, entityName);
                } catch (final Exception exception) {
                    throw new MojoExecutionException(ERROR_CODE, exception);
                }

                try {
                    ctClassToModify.addMethod(withMethod);
                } catch (final CannotCompileException exception) {
                    throw new MojoExecutionException(ERROR_CODE, exception);
                }
            }
        }

        generateBuilderConstructorMethod(ctClassToModify);
        byte[] bytecode = null;
        try {
            bytecode = ctClassToModify.toBytecode();
        } catch (final Exception exception) {
            throw new MojoExecutionException(ERROR_CODE, exception);
        }
        return bytecode;
    }

    private void importPackage(ClassPool classPool, String packageName) {
        @SuppressWarnings("unchecked")
        final Iterator<String> importedPackages = classPool.getImportedPackages();
        while (importedPackages.hasNext()) {
            final String next = importedPackages.next();
            if (next.equals(packageName)) {
                return;
            }
        }
        classPool.importPackage(packageName);
    }

    private String packageName(NotFoundException exception) {
        final String notImportedPackageName = exception.getLocalizedMessage();
        final String[] splittedName = notImportedPackageName.split("\\.");
        return notImportedPackageName.substring(0,
                notImportedPackageName.indexOf(splittedName[splittedName.length - 1]) - 1);
    }

    private CtMethod generateMethodBodyWithBuilder(ClassPool classPool, CtClass toModify, final CtMethod ctMethod,
            String entityName) throws MojoExecutionException, CannotCompileException {
        CtClass[] parameterTypes = null;
        CtClass[] exceptionTypes = null;
        try {
            parameterTypes = ctMethod.getParameterTypes();
            exceptionTypes = ctMethod.getExceptionTypes();
        } catch (final NotFoundException exception) {
            classPool.importPackage(packageName(exception));
            try {
                parameterTypes = ctMethod.getParameterTypes();
                exceptionTypes = ctMethod.getExceptionTypes();
            } catch (final NotFoundException innerException) {
                throw new MojoExecutionException(ERROR_CODE + " On method compile: " + exception.getLocalizedMessage()
                        + ". Did you add all the dependencies of this jar? Dependency:"
                        + innerException.getLocalizedMessage());
            }
        }

        return CtNewMethod.make(ctMethod.getDeclaringClass(), "with" + entityName, parameterTypes, exceptionTypes,
                generateBuilderMethodBody(entityName, parameterTypes.length), ctMethod.getDeclaringClass());
    }

    private void generateBuilderConstructorMethod(CtClass toModify) throws MojoExecutionException {
        CtMethod m;
        try {
            m = CtNewMethod.make(generateBuilderConstructorMethodBody(toModify), toModify);
            toModify.addMethod(m);
            toModify.writeFile();
        } catch (final Exception exception) {
            throw new MojoExecutionException(ERROR_CODE, exception);
        }
    }

    private String generateBuilderConstructorMethodBody(CtClass toModify) {
        final StringBuilder constructorBuilder = new StringBuilder();
        constructorBuilder.append("public static synchronized ").append(toModify.getSimpleName()).append(" create() {");
        constructorBuilder.append("return new ").append(toModify.getSimpleName()).append("();").append("}");
        final String constructorMethod = constructorBuilder.toString();
        return constructorMethod;
    }

    private String generateBuilderMethodBody(String entityName, int parametersSize) {
        final StringBuilder constructorBuilder = new StringBuilder();
        constructorBuilder.append("{");
        constructorBuilder.append("this.set").append(entityName).append("(");
        int index = 1;
        constructorBuilder.append("$" + index);
        for (index = 2; index < parametersSize; index++) {
            constructorBuilder.append(",$" + index++);
        }
        constructorBuilder.append(");");
        constructorBuilder.append("return this;");
        constructorBuilder.append("}");
        return constructorBuilder.toString();
    }

}
