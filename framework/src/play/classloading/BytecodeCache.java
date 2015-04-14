package play.classloading;

import org.apache.commons.io.FileUtils;
import play.Logger;
import play.Play;
import play.PlayPlugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.MessageDigest;

import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;

/**
 * Used to speed up compilation time
 */
public class BytecodeCache {

    /**
     * Delete the bytecode
     * @param name Cache name
     */
    public static void deleteBytecode(String name) {
        try {
            if (!isBytecodeCacheEnabled() || Play.readOnlyTmp) {
                return;
            }
            cacheFile(cacheFileName(name)).delete();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve the bytecode if source has not changed
     * @param name The cache name
     * @param source The source
     * @return The bytecode
     */
    public static byte[] getBytecode(String name, String source) {
        try {
            if (isBytecodeCacheEnabled()) {
                return null;
            }
            File f = cacheFile(cacheFileName(name));
            if (f.exists()) {
                if (false) {
                  // TODO check for modification
//                if (f.lastModified() < javaSource.lastModified()) {
//                  Logger.trace("Bytecode too old (%s < %s)", f.lastModified(), javaSource.lastModified());
//                  deleteBytecode(name);
//                  return null;
                }
                else {
                  return readFileToByteArray(f);
                }
            }

            if (Logger.isTraceEnabled()) {
                Logger.trace("Cache MISS for %s", name);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String cacheFileName(String name) {
      return name.replace("/", "_").replace("{", "_").replace("}", "_").replace(":", "_");
    }

    /**
     * Cache the bytecode
     * @param byteCode The bytecode
     * @param name The cache name
     * @param source The corresponding source
     */
    public static void cacheBytecode(byte[] byteCode, String name, String source) {
        try {
            if (isBytecodeCacheEnabled() || Play.readOnlyTmp) {
                return;
            }
            String cacheFileName = cacheFileName(name);
            FileUtils.writeByteArrayToFile(cacheFile(cacheFileName), byteCode);

            // emit bytecode to standard class layout as well
            // TODO Remove it. I guess it's not used.
            if (!name.contains("/") && !name.contains("{")) {
                File f = new File(Play.tmpDir, "classes/" + name.replace(".", "/") + ".class");
                f.getParentFile().mkdirs();
                writeByteArrayToFile(f, byteCode);
            }

            if (Logger.isTraceEnabled()) {
                Logger.trace("%s cached", name);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
  
    private static boolean isBytecodeCacheEnabled() {
        return Play.tmpDir == null || !Play.configuration.getProperty("play.bytecodeCache", "true").equals("true");
    }

    /**
     * Build a hash of play version and enabled plugins
     */
    static String hash() {
        if (!Play.initialized) {
          return Play.version + Play.mode.name();
        }
        try {
            StringBuilder plugins = new StringBuilder();
            plugins.append(Play.version);
            plugins.append(Play.mode.name());
            plugins.append(Play.initialized);
            if (Play.initialized) {
              for (PlayPlugin plugin : Play.pluginCollection.getEnabledPlugins()) {
                plugins.append(plugin.getClass().getName());
              }
            }
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(plugins.toString().getBytes("utf-8"));
            byte[] digest = messageDigest.digest();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < digest.length; ++i) {
                int value = digest[i];
                if (value < 0) {
                    value += 256;
                }
                builder.append(Integer.toHexString(value));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve the real file that will be used as cache.
     */
    static File cacheFile(String className) {
        File dir = getCacheDir();
        return new File(dir, className);
    }

    static File getCacheDir() {
        File dir = new File(Play.tmpDir, "bytecode/" + hash());
        if (!dir.exists() && Play.tmpDir != null && !Play.readOnlyTmp) {
            dir.mkdirs();
        }
        return dir;
    }
  
    public static File[] getCachedClasses() {
        File dir = getCacheDir();
        return dir.listFiles(new FilenameFilter() {
          @Override public boolean accept(File dir, String name) {
            return !name.endsWith(".html") && !name.endsWith(".js") && !name.endsWith(".tag");
          }
        });
    }
  
    public static void cleanCache() {
        try {
            FileUtils.cleanDirectory(getCacheDir());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
