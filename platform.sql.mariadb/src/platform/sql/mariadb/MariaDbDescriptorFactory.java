package platform.sql.mariadb;

import platform.sql.DatabaseDescriptor;
import platform.sql.IDatabaseDescriptorFactory;

public class MariaDbDescriptorFactory
        implements IDatabaseDescriptorFactory {
    
    @Override
    public DatabaseDescriptor create(final String url, final String db, final String user, final String password) {
        return new MariaDbDescriptor(url, db, user, password);
    }
    
    @Override
    public boolean handle(final String url) {
        return url.contains(":mysql:"); //$NON-NLS-1$
    }
    
}
