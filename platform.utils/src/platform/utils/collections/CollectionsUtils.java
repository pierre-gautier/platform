package platform.utils.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionsUtils {
    
    @SuppressWarnings("unchecked")
    public static <T> List<T> asList(final T... elements) {
        if (elements == null) {
            return new ArrayList<>(0);
        }
        final List<T> list = new ArrayList<>(elements.length);
        for (final T element : elements) {
            if (element != null) {
                list.add(element);
            }
        }
        return list;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Set<T> asSet(final T... elements) {
        if (elements == null) {
            return new HashSet<>(0);
        }
        final Set<T> set = new HashSet<>(elements.length);
        for (final T element : elements) {
            if (element != null) {
                set.add(element);
            }
        }
        return set;
    }
    
    public static final <T> List<T> createList(final int initialCapacity) {
        return new HashedArrayList<>(initialCapacity);
    }
    
    public static final <K, V> Map<K, V> createMap(final int initialCapacity, final V defaultValue) {
        if (defaultValue == null) {
            return new HashMap<>(initialCapacity);
        }
        return new DefaultValueHashMap<>(initialCapacity, defaultValue);
    }
    
    public static final <K, V, C extends Collection<V>> Map<K, C> createMultiMap(final int initialCapacity, final Class<C> defaultValue) {
        if (defaultValue == null) {
            return new HashMap<>(initialCapacity);
        }
        return new MultiMap<>(initialCapacity, defaultValue);
    }
    
    public static <T> List<List<T>> sub(final List<T> list, final int size) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        if (size < 1) {
            return Arrays.asList(list);
        }
        final int number = list.size();
        final List<List<T>> subs = new ArrayList<>(number);
        for (int i = 0; i < number; i += size) {
            subs.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return subs;
        
    }
    
    private CollectionsUtils() {
        // hide constructor
    }
    
}
