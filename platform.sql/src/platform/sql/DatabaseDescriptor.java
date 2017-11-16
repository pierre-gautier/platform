package platform.sql;

import platform.utils.Strings;

public abstract class DatabaseDescriptor {
    
    private final String db;
    private final String url;
    private final String user;
    private final String password;
    
    public DatabaseDescriptor(final String url, final String db, final String user, final String password) {
        super();
        this.db = db;
        this.url = url;
        this.user = user;
        this.password = password;
    }
    
    public boolean alwaysUseDb() {
        return false;
    }
    
    public String getCharset() {
        return Strings.EMPTY;
    }
    
    public String getDb() {
        return this.db;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public String getUser() {
        return this.user;
    }
    
    public String quote() {
        return "`"; //$NON-NLS-1$
    }
    
}
