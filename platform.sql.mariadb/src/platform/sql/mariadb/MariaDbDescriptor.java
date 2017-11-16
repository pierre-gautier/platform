package platform.sql.mariadb;

import platform.sql.DatabaseDescriptor;

public class MariaDbDescriptor
        extends DatabaseDescriptor {
    
    public MariaDbDescriptor(final String url, final String db, final String user, final String password) {
        super(url, db, user, password);
    }
    
    @Override
    public String getCharset() {
        return " DEFAULT CHARACTER SET utf8 "; //$NON-NLS-1$
    }
    
    @Override
    public String quote() {
        return "`"; //$NON-NLS-1$
    }
    
}
