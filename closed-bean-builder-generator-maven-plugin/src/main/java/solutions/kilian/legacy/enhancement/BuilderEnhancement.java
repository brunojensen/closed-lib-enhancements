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

public class BuilderEnhancement implements Enhancement {

    private Log log;
    private static final String ERROR_CODE = "[ERROR]";
    private final ClassPool classPool;

    public BuilderEnhancement(final Log log, final ClassPool classPool) {
        this.log = log;
        this.classPool = classPool;
    }

    @Override
    public void enhance(final EnhanceableFile enhanceableFile) throws MojoExecutionException {
        try {
            for (final EnhanceableEntry enhanceableEntry : enhanceableFile.getEntries()) {
                log.info(enhanceableEntry.getCanonicalName());
                final CtClass ctClass = classPool.get(enhanceableEntry.getCanonicalName());
                byte[] bytecode = null;
                if (!ctClass.isEnum() && !ctClass.isInterface() && !ctClass.isPrimitive()) {
                    importPackage(classPool, ctClass.getPackageName());
                    bytecode = transformClass(ctClass, classPool);
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
                String entityName = ctMethod.getName().substring(3);
                CtMethod withMethod = null;
                try {
                    withMethod = generateMethodBodyWithBuilder(classPool, ctClassToModify, ctMethod, entityName);
                } catch (Exception exception) {
                    throw new MojoExecutionException(ERROR_CODE, exception);
                }

                try {
                    ctClassToModify.addMethod(withMethod);
                } catch (CannotCompileException exception) {
                    throw new MojoExecutionException(ERROR_CODE, exception);
                }
            }
        }

        generateBuilderConstructorMethod(ctClassToModify);
        byte[] bytecode = null;
        try {
            bytecode = ctClassToModify.toBytecode();
        } catch (Exception exception) {
            throw new MojoExecutionException(ERROR_CODE, exception);
        }
        return bytecode;
    }

    private void importPackage(ClassPool classPool, String packageName) {
        @SuppressWarnings("unchecked")
        Iterator<String> importedPackages = classPool.getImportedPackages();
        while (importedPackages.hasNext()) {
            String next = (String) importedPackages.next();
            if (next.equals(packageName)) {
                return;
            }
        }
        classPool.importPackage(packageName);
    }

    private String packageName(NotFoundException exception) {
        String notImportedPackageName = exception.getLocalizedMessage();
        String[] splittedName = notImportedPackageName.split("\\.");
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
        } catch (NotFoundException exception) {
            classPool.importPackage(packageName(exception));
            try {
                parameterTypes = ctMethod.getParameterTypes();
                exceptionTypes = ctMethod.getExceptionTypes();
            } catch (NotFoundException innerException) {
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
        } catch (Exception exception) {
            throw new MojoExecutionException(ERROR_CODE, exception);
        }
    }

    private String generateBuilderConstructorMethodBody(CtClass toModify) {
        StringBuilder constructorBuilder = new StringBuilder();
        constructorBuilder.append("public static synchronized ").append(toModify.getSimpleName()).append(" create() {");
        constructorBuilder.append("return new ").append(toModify.getSimpleName()).append("();").append("}");
        String constructorMethod = constructorBuilder.toString();
        return constructorMethod;
    }

    private String generateBuilderMethodBody(String entityName, int parametersSize) {
        StringBuilder constructorBuilder = new StringBuilder();
        constructorBuilder.append("{");
        constructorBuilder.append("this.set").append(entityName).append("(");
        int index = 1;
        constructorBuilder.append("$" + (index));
        for (index = 2; index < parametersSize; index++) {
            constructorBuilder.append(",$" + (index++));
        }
        constructorBuilder.append(");");
        constructorBuilder.append("return this;");
        constructorBuilder.append("}");
        return constructorBuilder.toString();
    }

}
