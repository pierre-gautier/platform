package platform.sql;

public interface IDatabaseDescriptorFactory {
    
    public DatabaseDescriptor create(final String url, final String db, final String user, final String password);
    
    boolean handle(String url);
    
}
