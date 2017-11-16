package platform.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

import platform.utils.Strings;

public class Descriptor<TYPE>
        implements Comparable<Descriptor<TYPE>> {
    
    private static final Map<String, Descriptor<?>> TYPES = new HashMap<>(128);
    
    @SuppressWarnings("unchecked")
    public static <T> Descriptor<T> getDescriptor(final String id) {
        return (Descriptor<T>) Descriptor.TYPES.get(id);
    }
    
    private final String      id;
    private final String      label;
    private final String      category;
    private final Class<TYPE> clazz;
    private final TYPE        defaulValue;
    
    public Descriptor(final String id, final String label, final Class<TYPE> clazz) {
        this(id, label, null, clazz, null);
    }
    
    public Descriptor(final String id, final String label, final String category, final Class<TYPE> clazz, final TYPE defaultValue) {
        Assert.isTrue(!Strings.isNullEmptyOrBlank(label, id), "id and label must not be null or empty"); //$NON-NLS-1$
        Assert.isTrue(Descriptor.TYPES.put(id, this) == null, id + " already exists"); //$NON-NLS-1$
        this.defaulValue = defaultValue;
        this.category = category;
        this.clazz = clazz;
        this.label = label;
        this.id = id;
    }
    
    @Override
    public int compareTo(final Descriptor<TYPE> o) {
        if (o == null) {
            return -1;
        }
        return this.label.compareTo(o.label);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Descriptor<?> other = (Descriptor<?>) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }
    
    public String getCategory() {
        return this.category;
    }
    
    public Class<TYPE> getClazz() {
        return this.clazz;
    }
    
    public TYPE getDefaulValue() {
        return this.defaulValue;
    }
    
    public String getId() {
        return this.id;
    }
    
    public String getLabel() {
        return this.label;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.id == null ? 0 : this.id.hashCode());
        return result;
    }
    
    @Override
    public String toString() {
        return this.label;
    }
    
}
