package platform.utils.collections;

import java.util.HashMap;

public final class DefaultValueHashMap<K, V>
        extends HashMap<K, V> {
    
    private static final long serialVersionUID = -7385206508585886243L;
    
    private final V           defaultValue;
    
    public DefaultValueHashMap(final int size, final V defaultValue) {
        super(size);
        this.defaultValue = defaultValue;
    }
    
    @Override
    public V get(final Object key) {
        final V value = super.get(key);
        if (value == null) {
            return this.defaultValue;
        }
        return value;
    }
    
    @Override
    public V put(final K key, final V value) {
        if (value == null || value == this.defaultValue) {
            return this.remove(key);
        }
        return super.put(key, value);
    }
    
}