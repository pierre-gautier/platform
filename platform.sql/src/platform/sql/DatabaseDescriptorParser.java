package platform.sql;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Properties;

@SuppressWarnings("nls")
public class DatabaseDescriptorParser {
    
    public static DatabaseDescriptor load(final InputStream in) {
        final Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
        final String db = properties.getProperty("db");
        final String url = properties.getProperty("url");
        final String user = properties.getProperty("user");
        final String password = properties.getProperty("password");
        return DatabaseDescriptorFactories.INSTANCE.create(url, db, user, password);
    }
    
    public static void save(final DatabaseDescriptor descriptor, final Writer out) {
        final Properties properties = new Properties();
        properties.put("db", descriptor.getDb()); //$NON-NLS-1$
        properties.put("url", descriptor.getUrl()); //$NON-NLS-1$
        properties.put("user", descriptor.getUser()); //$NON-NLS-1$
        properties.put("password", descriptor.getPassword()); //$NON-NLS-1$
        try {
            properties.store(out, null);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
    
}
