package platform.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.Assert;

import platform.utils.Strings;

@SuppressWarnings("nls")
public abstract class AObject
        implements IObject {
    
    private final String                        id;
    private final Descriptor<? extends IObject> type;
    private final Map<Descriptor<?>, Object>    attributes;
    private final IRoot                         root;
    
    private Collection<IAttributeListener>      listeners;
    
    protected AObject(final Descriptor<? extends IObject> type, final String id, final Collection<Attribute> attributes, final IRoot root) {
        Assert.isTrue(type != null, "type must not be null");
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.type = type;
        this.root = root;
        this.attributes = new HashMap<>(1);
        this.addAttributes(this.getDefaultAttributes());
        if (attributes != null) {
            this.addAttributes(attributes);
        }
    }
    
    @Override
    public final <T> void addAttribute(final Descriptor<T> descriptor, final T value) {
        final Collection<Attribute> list = new ArrayList<>(1);
        list.add(new Attribute(descriptor, value));
        this.addAttributes(list);
    }
    
    @Override
    public final void addAttributes(final Collection<Attribute> values) {
        for (final IStrategy strategy : this.getRoot().getStrategies()) {
            strategy.addAttributes(this, values);
        }
        final Collection<Attribute> modifiedValues = new HashSet<>(values.size());
        final Collection<Descriptor<?>> descriptorsToRemove = new ArrayList<>(values.size());
        for (final Attribute value : values) {
            if (value.getValue() == null || Strings.isNullEmptyOrBlank(value.getValue().toString())) {
                descriptorsToRemove.add(value.getDescriptor());
                continue;
            }
            descriptorsToRemove.remove(value.getDescriptor());
            final Object modifiedValue = this.attributes.put(value.getDescriptor(), value.getValue());
            if (!Objects.equals(modifiedValue, value.getValue())) {
                modifiedValues.add(Attribute.unchecked(value.getDescriptor(), value.getValue()));
            }
        }
        for (final Descriptor<?> descriptor : descriptorsToRemove) {
            final Object value = this.attributes.remove(descriptor);
            if (value != null) {
                modifiedValues.add(Attribute.unchecked(descriptor, value));
            }
        }
        this.sendEvent(modifiedValues);
    }
    
    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof IObject)) {
            return false;
        }
        return Objects.equals(this.id, ((IObject) obj).getId());
    }
    
    @Override
    public final <T> T getAttribute(final Descriptor<T> descriptor) {
        Assert.isTrue(descriptor != null, "descriptor must not be null");
        Object value = this.attributes.get(descriptor);
        if (value == null) {
            value = descriptor.getDefaulValue();
        }
        for (final IStrategy strategy : this.getRoot().getStrategies()) {
            final T strategyValue = strategy.getAttribute(this, descriptor, value);
            if (strategyValue != null) {
                value = strategyValue;
            }
        }
        return descriptor.getClazz().cast(value);
    }
    
    @Override
    public final Collection<Attribute> getAttributes() {
        final List<Attribute> values = new ArrayList<>(this.attributes.size());
        for (final Entry<Descriptor<?>, Object> entry : this.attributes.entrySet()) {
            values.add(Attribute.unchecked(entry.getKey(), entry.getValue()));
        }
        return values;
    }
    
    @Override
    public Collection<Attribute> getDefaultAttributes() {
        return new ArrayList<>();
    }
    
    @Override
    public final String getId() {
        return this.id;
    }
    
    @Override
    public IRoot getRoot() {
        return this.root;
    }
    
    @Override
    public Descriptor<? extends IObject> getType() {
        return this.type;
    }
    
    @Override
    public final int hashCode() {
        return this.id.hashCode();
    }
    
    @Override
    public final void registerAttributeListener(final IAttributeListener objectListener) {
        if (objectListener == null) {
            return;
        }
        if (this.listeners == null) {
            this.listeners = new CopyOnWriteArrayList<>();
        }
        this.listeners.add(objectListener);
    }
    
    @Override
    public final String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append(" type=");
        builder.append(this.getType());
        builder.append(", id=");
        builder.append(this.getId());
        return builder.toString();
    }
    
    @Override
    public final void unregisterAttributeListener(final IAttributeListener objectListener) {
        if (this.listeners == null || objectListener == null) {
            return;
        }
        this.listeners.remove(objectListener);
        if (this.listeners.isEmpty()) {
            this.listeners = null;
        }
    }
    
    private void sendEvent(final Collection<Attribute> modifiedValues) {
        if (modifiedValues.isEmpty()) {
            return;
        }
        for (final IStrategy strategy : this.getRoot().getStrategies()) {
            strategy.attributesChanged(this, modifiedValues);
        }
        if (this.listeners != null) {
            for (final IAttributeListener listener : this.listeners) {
                listener.attributesChanged(this, modifiedValues);
            }
        }
    }
    
}
