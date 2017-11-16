package platform.utils.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

public class HashedArrayList<T>
        extends ArrayList<T> {
    
    private static final long               serialVersionUID = -7827380207842473120L;
    
    private final transient Map<T, Integer> objectToIndex;
    
    public HashedArrayList(final Collection<? extends T> collection) {
        this(collection.size());
        this.addAll(collection);
    }
    
    public HashedArrayList(final int size) {
        super(size);
        this.objectToIndex = new HashMap<>(size);
    }
    
    @Override
    public void add(final int index, final T element) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean add(final T element) {
        Assert.isTrue(element != null, "added element must not be null"); //$NON-NLS-1$
        if (this.objectToIndex.containsKey(element)) {
            return false;
        }
        final boolean added = super.add(element);
        this.objectToIndex.put(element, Integer.valueOf(this.size() - 1));
        return added;
    }
    
    @Override
    public boolean addAll(final Collection<? extends T> c) {
        // crappy implementation, it may resize the backing array many times, improve if needed
        boolean modified = false;
        for (final T object : c) {
            modified |= this.add(object);
        }
        return modified;
    }
    
    @Override
    public boolean addAll(final int index, final Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void clear() {
        super.clear();
        this.objectToIndex.clear();
    }
    
    @Override
    public Object clone() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean contains(final Object o) {
        return this.objectToIndex.containsKey(o);
    }
    
    @Override
    public int indexOf(final Object o) {
        final Integer index = this.objectToIndex.get(o);
        return index == null ? -1 : index.intValue();
    }
    
    @Override
    public int lastIndexOf(final Object o) {
        return this.indexOf(o);
    }
    
    @Override
    public T remove(final int index) {
        final T removed = super.remove(index);
        this.objectToIndex.remove(removed);
        this.reindex(index);
        return removed;
    }
    
    @Override
    public boolean remove(final Object o) {
        final Integer indexObject = this.objectToIndex.get(o);
        if (indexObject == null) {
            return false;
        }
        return this.remove(indexObject.intValue()) != null;
    }
    
    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public T set(final int index, final T element) {
        Assert.isTrue(element != null, "element must not be null"); //$NON-NLS-1$
        final T former = super.set(index, element);
        if (former.equals(element)) {
            return former;
        }
        final Integer indexOfElement = this.objectToIndex.put(element, index);
        if (indexOfElement != null) {
            this.set(indexOfElement.intValue(), former);
        } else {
            this.objectToIndex.remove(former);
        }
        return former;
    }
    
    protected void reindex(final int index) {
        for (int i = index; i < this.size(); i++) {
            this.objectToIndex.put(this.get(i), i);
        }
    }
}
