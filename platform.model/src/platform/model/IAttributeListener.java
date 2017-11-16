package platform.model;

import java.util.Collection;

public interface IAttributeListener {
    
    void attributesChanged(IObject object, Collection<Attribute> attributes);
    
}
