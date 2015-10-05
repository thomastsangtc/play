package play.classloading.compiler;

import play.vfs.VirtualFile;

public class JavaSourceFile {
    public final VirtualFile javaFile;
    public final String compilationUnitName; // e.g. "plugins/MyPlugin.java"
    public final String className; // e.g. "plugins.MyPlugin"
    public final String packageName; // e.g. "plugins"
    public final String typeName; // e.g. "MyPlugin"

    public JavaSourceFile(VirtualFile javaFile, String compilationUnitName) {
        this.javaFile = javaFile;
        this.compilationUnitName = compilationUnitName;
        int indexOfSlash = compilationUnitName.lastIndexOf('/');
        this.packageName = indexOfSlash < 0 ? "" : compilationUnitName.substring(0, indexOfSlash).replace('/', '.');
        this.typeName = javaFile.getName().substring(0, javaFile.getName().length() - 5); // remove ".java"
        this.className = packageName.isEmpty() ? typeName : packageName + '.' + typeName;
    }

    @Override
    public int hashCode() {
        return compilationUnitName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof JavaSourceFile)) return false;
        return compilationUnitName.equals(((JavaSourceFile) obj).compilationUnitName);
    }
}
    