package platform.sql.h2;

import platform.sql.DatabaseDescriptor;

public class H2Descriptor
        extends DatabaseDescriptor {
    
    public H2Descriptor(final String url, final String db, final String user, final String password) {
        super(url, db, user, password);
    }
    
    @Override
    public boolean alwaysUseDb() {
        return true;
    }
    
}
