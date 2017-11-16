package platform.model;

import org.eclipse.core.runtime.Assert;

@SuppressWarnings("nls")
public final class Attribute {
    
    @SuppressWarnings("unchecked")
    public static Attribute unchecked(final Descriptor<?> descriptor, final Object value) {
        return new Attribute((Descriptor<Object>) descriptor, value);
    }
    
    private final Descriptor<?> descriptor;
    private final Object        value;
    
    public <T> Attribute(final Descriptor<T> descriptor, final T value) {
        Assert.isTrue(descriptor != null, "descriptor cannot be null");
        this.descriptor = descriptor;
        this.value = value;
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
        final Attribute other = (Attribute) obj;
        if (!this.descriptor.equals(other.descriptor)) {
            return false;
        }
        if (this.value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!this.value.equals(other.value)) {
            return false;
        }
        return true;
    }
    
    public Descriptor<?> getDescriptor() {
        return this.descriptor;
    }
    
    public Object getValue() {
        return this.value;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.descriptor.hashCode();
        result = prime * result + (this.value == null ? 0 : this.value.hashCode());
        return result;
    }
    
    @Override
    public String toString() {
        return this.descriptor + "=" + this.value;
    }
    
}
