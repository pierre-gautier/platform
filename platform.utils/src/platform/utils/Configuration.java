package platform.utils;

import java.io.File;

public class Configuration {
    
    public static File application() {
        final String path = System.getProperty("osgi.instance.area"); //$NON-NLS-1$
        final File file = new File(path.replaceAll("file:/", "")); //$NON-NLS-1$ //$NON-NLS-2$
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }
    
    public static File file(final String name) {
        return new File(Configuration.application(), name);
    }
    
}
