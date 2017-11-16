package platform.model.io;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

import platform.utils.Strings;

@SuppressWarnings("nls")
public class Formatter<TYPE> {
    
    private static final Map<Class<?>, Formatter<?>> FORMATTERS = new HashMap<>(32);
    
    @SuppressWarnings("unchecked")
    public static final <T> Formatter<T> getFormatter(final Class<T> clazz) {
        return (Formatter<T>) Formatter.FORMATTERS.get(clazz);
    }
    
    public Formatter(final Class<TYPE> clazz) {
        Assert.isTrue(clazz != null, "clazz cannot be null");
        Assert.isTrue(Formatter.FORMATTERS.put(clazz, this) == null, "already found formatter for clazz " + clazz);
    }
    
    public String toString(final TYPE input) {
        if (input == null) {
            return Strings.EMPTY;
        }
        return input.toString();
    }
    
}
