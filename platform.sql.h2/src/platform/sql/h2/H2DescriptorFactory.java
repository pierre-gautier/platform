package platform.sql.h2;

import platform.sql.DatabaseDescriptor;
import platform.sql.IDatabaseDescriptorFactory;

public class H2DescriptorFactory
        implements IDatabaseDescriptorFactory {
    
    @Override
    public DatabaseDescriptor create(final String url, final String db, final String user, final String password) {
        return new H2Descriptor(url, db, user, password);
    }
    
    @Override
    public boolean handle(final String url) {
        return url.contains(":h2:"); //$NON-NLS-1$
    }
    
}
