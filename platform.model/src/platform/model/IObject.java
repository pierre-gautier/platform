package platform.model;

import java.util.Collection;

public interface IObject {
    
    <T> void addAttribute(Descriptor<T> descriptor, T value);
    
    void addAttributes(Collection<Attribute> values);
    
    <T> T getAttribute(final Descriptor<T> descriptor);
    
    Collection<Attribute> getAttributes();
    
    Collection<Attribute> getDefaultAttributes();
    
    String getId();
    
    IRoot getRoot();
    
    Descriptor<? extends IObject> getType();
    
    void registerAttributeListener(IAttributeListener listener);
    
    void unregisterAttributeListener(IAttributeListener listener);
    
}
