package solutions.kilian.legacy.entry;

import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;

public class EnhanceableEntry extends JarEntry {
    private static final String DOT = ".";
    public static final String DIRECTORY_SEPARATOR = "/";
    private static final String CLASS_SUFFIX = ".class";
    private byte[] byteCode;

    public EnhanceableEntry(JarEntry je) {
        super(je);
    }

    public EnhanceableEntry(String name) {
        super(name);
    }

    public EnhanceableEntry(ZipEntry ze) {
        super(ze);
    }

    public String getCanonicalName() {
        return super.getName().replace(DIRECTORY_SEPARATOR, DOT).replace(CLASS_SUFFIX, "");
    }

    public boolean isClassFile() {
        return super.getName().endsWith(CLASS_SUFFIX);
    }

    public byte[] getByteCode() {
        return byteCode;
    }

    public void setByteCode(byte[] byteCode) {
        this.byteCode = byteCode;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EnhanceableEntry other = (EnhanceableEntry) obj;
        return super.getName().equals(other.getName());
    }


}
