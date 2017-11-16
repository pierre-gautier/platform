package platform.ui.preferences;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.IRoot;

public class NodePreferenceStore
        implements IPersistentPreferenceStore {
    
    private final IRoot node;
    
    public NodePreferenceStore(final IRoot node) {
        this.node = node;
    }
    
    @Override
    public void addPropertyChangeListener(final IPropertyChangeListener listener) {
        throw new IllegalAccessError();
    }
    
    @Override
    public boolean contains(final String name) {
        return true;
    }
    
    @Override
    public void firePropertyChangeEvent(final String name, final Object oldValue, final Object newValue) {
        throw new IllegalAccessError();
    }
    
    @Override
    public boolean getBoolean(final String name) {
        return this.get(name, Boolean.class);
    }
    
    @Override
    public boolean getDefaultBoolean(final String name) {
        return this.getDefault(name, Boolean.class);
    }
    
    @Override
    public double getDefaultDouble(final String name) {
        return this.getDefault(name, Double.class);
    }
    
    @Override
    public float getDefaultFloat(final String name) {
        return this.getDefault(name, Float.class);
    }
    
    @Override
    public int getDefaultInt(final String name) {
        return this.getDefault(name, Integer.class);
    }
    
    @Override
    public long getDefaultLong(final String name) {
        return this.getDefault(name, Long.class);
    }
    
    @Override
    public String getDefaultString(final String name) {
        return this.getDefault(name, String.class);
    }
    
    @Override
    public double getDouble(final String name) {
        return this.get(name, Double.class);
    }
    
    @Override
    public float getFloat(final String name) {
        return this.get(name, Float.class);
    }
    
    @Override
    public int getInt(final String name) {
        return this.get(name, Integer.class);
    }
    
    @Override
    public long getLong(final String name) {
        return this.get(name, Long.class);
    }
    
    @Override
    public String getString(final String name) {
        return this.get(name, String.class);
    }
    
    @Override
    public boolean isDefault(final String name) {
        final Descriptor<?> descriptor = Descriptor.getDescriptor(name);
        if (descriptor == null) {
            return false;
        }
        return Objects.equals(this.get(name, descriptor.getClazz()), this.getDefault(name, descriptor.getClazz()));
    }
    
    @Override
    public boolean needsSaving() {
        return true;
    }
    
    @Override
    public void putValue(final String name, final String value) {
        throw new IllegalAccessError();
    }
    
    @Override
    public void removePropertyChangeListener(final IPropertyChangeListener listener) {
        throw new IllegalAccessError();
    }
    
    @Override
    public void save() throws IOException {
        // do nothing
    }
    
    @Override
    public void setDefault(final String name, final boolean value) {
        throw new IllegalAccessError();
    }
    
    @Override
    public void setDefault(final String name, final double value) {
        throw new IllegalAccessError();
    }
    
    @Override
    public void setDefault(final String name, final float value) {
        throw new IllegalAccessError();
    }
    
    @Override
    public void setDefault(final String name, final int value) {
        throw new IllegalAccessError();
    }
    
    @Override
    public void setDefault(final String name, final long value) {
        throw new IllegalAccessError();
    }
    
    @Override
    public void setDefault(final String name, final String defaultObject) {
        throw new IllegalAccessError();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void setToDefault(final String name) {
        final Descriptor<?> descriptor = Descriptor.getDescriptor(name);
        if (descriptor == null) {
            return;
        }
        final Class<Object> clazz = (Class<Object>) descriptor.getClazz();
        final Object defaultValue = this.getDefault(name, clazz);
        this.setValue(name, clazz, defaultValue);
    }
    
    @Override
    public void setValue(final String name, final boolean value) {
        this.setValue(name, Boolean.class, value);
    }
    
    @Override
    public void setValue(final String name, final double value) {
        this.setValue(name, Double.class, value);
    }
    
    @Override
    public void setValue(final String name, final float value) {
        this.setValue(name, Float.class, value);
    }
    
    @Override
    public void setValue(final String name, final int value) {
        this.setValue(name, Integer.class, value);
    }
    
    @Override
    public void setValue(final String name, final long value) {
        this.setValue(name, Long.class, value);
    }
    
    @Override
    public void setValue(final String name, final String value) {
        this.setValue(name, String.class, value);
    }
    
    private <T> T get(final String name, final Class<T> clazz) {
        final Descriptor<T> descriptor = Descriptor.getDescriptor(name);
        if (descriptor == null || descriptor.getClazz() != clazz) {
            return null;
        }
        return this.node.getAttribute(descriptor);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getDefault(final String name, final Class<T> clazz) {
        final Descriptor<T> descriptor = Descriptor.getDescriptor(name);
        if (descriptor == null || descriptor.getClazz() != clazz) {
            return null;
        }
        final Collection<Attribute> defaultAttributes = this.node.getDefaultAttributes();
        for (final Attribute defaultAttribute : defaultAttributes) {
            if (defaultAttribute.getDescriptor() == descriptor) {
                return (T) defaultAttribute.getValue();
            }
        }
        return descriptor.getDefaulValue();
    }
    
    private <T> void setValue(final String name, final Class<T> clazz, final T value) {
        final Descriptor<T> descriptor = Descriptor.getDescriptor(name);
        if (descriptor == null || descriptor.getClazz() != clazz) {
            return;
        }
        this.node.addAttribute(descriptor, value);
    }
    
}
