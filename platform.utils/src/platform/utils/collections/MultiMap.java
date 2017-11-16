package platform.utils.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class MultiMap<K, V, C extends Collection<V>>
        extends HashMap<K, C> {
    
    private static final long serialVersionUID = 1487441087581756017L;
    private final Class<C>    clazz;
    
    public MultiMap(final int size, final Class<C> clazz) {
        super(size);
        this.clazz = clazz;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public C get(final Object key) {
        C value = super.get(key);
        if (value == null) {
            try {
                value = this.clazz.newInstance();
                this.put((K) key, value);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return value;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void putAll(final Map<? extends K, ? extends C> m) {
        for (final Iterator<?> iterator = m.entrySet().iterator(); iterator.hasNext();) {
            final Map.Entry<? extends K, ? extends C> entry = (Map.Entry<? extends K, ? extends C>) iterator.next();
            final C value = super.get(entry.getKey());
            if (value != null) {
                value.addAll(entry.getValue());
                iterator.remove();
            }
        }
        super.putAll(m);
    }
}