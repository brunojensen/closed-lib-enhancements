package solutions.kilian.legacy.entry;

import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;

public class EnhanceableEntry extends JarEntry {
    private static final String EMPTY = "";
    public static final String DIRECTORY_SEPARATOR = "/";
    private static final String CLASS_SUFFIX = ".class";

    public EnhanceableEntry(JarEntry je) {
        super(je);
    }

    public EnhanceableEntry(String name) {
        super(name);
    }

    public EnhanceableEntry(ZipEntry ze) {
        super(ze);
    }


    public String getSimpleName() {
        final String[] splitted = super.getName().split(DIRECTORY_SEPARATOR);
        return splitted[splitted.length - 1].replace(CLASS_SUFFIX, EMPTY);
    }

    @Override
    public String getName() {
        return super.getName().replace(DIRECTORY_SEPARATOR, ".").replace(CLASS_SUFFIX, EMPTY);
    }

}
