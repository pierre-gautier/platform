package platform.model.io;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.utils.Strings;

@SuppressWarnings("nls")
public abstract class Serializer<TYPE> {
    
    private static final Map<Class<?>, Serializer<?>> SERIALIZERS = new HashMap<>(32);
    
    public static Attribute deserialize(final String name, final String value) {
        if (name.equals(Strings.ID) || name.equals(Strings.TYPE)) {
            return null;
        }
        final Descriptor<?> descriptor = Descriptor.getDescriptor(name);
        if (descriptor == null) {
            return null;
        }
        final Serializer<?> serializer = Serializer.SERIALIZERS.get(descriptor.getClazz());
        if (serializer == null) {
            return null;
        }
        return Attribute.unchecked(descriptor, serializer.toObject(value));
    }
    
    public static String serialize(final Attribute attribute) {
        if (attribute == null) {
            return null;
        }
        final Serializer<?> serializer = Serializer.SERIALIZERS.get(attribute.getDescriptor().getClazz());
        if (serializer == null) {
            return null;
        }
        return serializer.toString(attribute.getValue());
    }
    
    public Serializer(final Class<TYPE> clazz) {
        Assert.isTrue(clazz != null, "serializer clazz cannot be null");
        Assert.isTrue(Serializer.SERIALIZERS.put(clazz, this) == null, "already found serializer for clazz " + clazz);
    }
    
    protected abstract TYPE toObject(String input);
    
    protected String toString(final Object input) {
        if (input == null) {
            return null;
        }
        return input.toString();
    }
    
}
