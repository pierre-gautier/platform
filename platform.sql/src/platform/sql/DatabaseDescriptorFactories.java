package platform.sql;

import java.util.Collection;
import java.util.HashSet;

public enum DatabaseDescriptorFactories
        implements IDatabaseDescriptorFactory {
    
    INSTANCE;
    
    private final Collection<IDatabaseDescriptorFactory> factories = new HashSet<>();
    
    @Override
    public final DatabaseDescriptor create(final String url, final String db, final String user, final String password) {
        for (final IDatabaseDescriptorFactory factory : this.factories) {
            if (factory.handle(url)) {
                return factory.create(url, db, user, password);
            }
        }
        return null;
    }
    
    @Override
    public boolean handle(final String url) {
        return this.factories.stream().anyMatch(f -> f.handle(url));
    }
    
    public void register(final IDatabaseDescriptorFactory factory) {
        this.factories.add(factory);
    }
    
}
