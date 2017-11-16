package platform.model.factory;

import java.util.Collection;

import platform.model.Descriptor;

public interface IFactory<T> {
    
    Collection<Descriptor<? extends T>> getSupportedTypes();
    
}
