package solutions.kilian.legacy.enhancement;

import javassist.ClassPool;

public abstract class AbstractEnhancement implements Enhancement {
    private final ClassPool classPool;

    public AbstractEnhancement(ClassPool classPool) {
        this.classPool = classPool;
    }

    public ClassPool getClassPool() {
        return classPool;
    }
}
